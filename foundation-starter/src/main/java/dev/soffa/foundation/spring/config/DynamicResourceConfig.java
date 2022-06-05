package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.commons.JavaUtil;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.resource.Resource;
import dev.soffa.foundation.spring.service.OperationDispatcher;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;

@Configuration
public class DynamicResourceConfig implements ApplicationContextAware {

    private static final Logger LOG = Logger.get(DynamicResourceConfig.class);

    private final AppConfig appConfig;
    private final OperationsMapping operationsMapping;

    private static final Set<String> LOADED = new HashSet<>();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DynamicResourceConfig(AppConfig appConfig, @Autowired(required = false) OperationsMapping operationsMapping) {
        this.appConfig = appConfig;
        this.operationsMapping = operationsMapping;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        if (operationsMapping == null || operationsMapping.isEmpty()) {
            LOG.warn("No operations was found on this project");
        } else {
            ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
            configure((DefaultListableBeanFactory) context.getBeanFactory());
        }
    }

    @SneakyThrows
    private void configure(DefaultListableBeanFactory beanFactory) {
        Reflections reflections = new Reflections(appConfig.getPkg());
        Set<Class<?>> resources =
            reflections.get(SubTypes.of(Resource.class).asClass().filter(c -> c.isAnnotationPresent(RestController.class) && c.isInterface()));

        if (CollectionUtil.isEmpty(resources)) {
            LOG.warn("No resources found in projet", resources.size());
            return;
        }

        LOG.info("Found %d resources", resources.size());

        Dispatcher dispatcher = new OperationDispatcher(operationsMapping);

        for (Class<?> clazz : resources) {
            String className = clazz.getName() + "Controller";
            if (!LOADED.contains(className)) {
                LOADED.add(className);
                MethodHandles.Lookup tmpInstance = null;

                if (JavaUtil.isJava8()) {
                    @SuppressWarnings("JavaReflectionMemberAccess")
                    Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(
                        Class.class, int.class
                    );
                    constructor.setAccessible(true);
                    tmpInstance = constructor.newInstance(clazz, MethodHandles.Lookup.PRIVATE);
                }

                final MethodHandles.Lookup instance = tmpInstance;

                Object controller = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(), // here is the trick
                    new Class[]{clazz}, (proxy, method, args) -> {
                        if (method.isDefault()) {

                            if (instance == null) {
                                return MethodHandles.lookup()
                                    .findSpecial(
                                        clazz,
                                        method.getName(),
                                        MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                                        clazz)
                                    .bindTo(proxy)
                                    .invokeWithArguments(args);
                            }

                            return instance.unreflectSpecial(method, clazz).
                                bindTo(proxy).
                                invokeWithArguments(args);
                        } else if ("invoke".equals(method.getName())) {
                            return MethodUtils.invokeMethod(dispatcher, "dispatch", args, method.getParameterTypes());
                        } else {
                            throw new UnsupportedOperationException("Method " + method.getName() + " is not supported");
                        }
                    });

                LOG.info("Dynamic resource registered: %s", className);

                beanFactory.registerSingleton(className, controller);
            }
        }
    }

}
