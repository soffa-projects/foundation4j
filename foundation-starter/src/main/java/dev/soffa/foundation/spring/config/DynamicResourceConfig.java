package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.ClassUtil;
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
import java.util.Set;

@Configuration
public class DynamicResourceConfig implements ApplicationContextAware {

    private static final Logger LOG = Logger.get(DynamicResourceConfig.class);

    private final AppConfig appConfig;
    private final OperationsMapping operationsMapping;

    public DynamicResourceConfig(AppConfig appConfig,
                                 @Autowired(required = false) OperationsMapping operationsMapping) {
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
        Set<Class<?>> resources = ClassUtil.findInterfacesAnnotatedWith(
            appConfig.getPkg(), RestController.class, Resource.class
        );

        if (CollectionUtil.isEmpty(resources)) {
            LOG.warn("No resources found in projet", resources.size());
            return;
        }

        LOG.info("Found %d resources", resources.size());

        Dispatcher dispatcher = new OperationDispatcher(operationsMapping);

        for (Class<?> clazz : resources) {
            String className = clazz.getName() + "Controller";

            // RolesAllowed classRolesAllowed = AnnotationUtils.findAnnotation(clazz, RolesAllowed.class);
            LOG.debug("Generating implement for %s", className);
            if (beanFactory.getSingleton(className) != null) {
                LOG.debug("%s is already loaded", className);
            } else {
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

                LOG.debug("Creating proxy for %s", className);

                //TODO: Cache RolesALlowed on this interface

                Object controller = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
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
                            // checkRoleAllowed(classRolesAllowed);
                            return MethodUtils.invokeMethod(dispatcher, "dispatch", args, method.getParameterTypes());
                        } else {
                            throw new UnsupportedOperationException("Method " + method.getName() + " is not supported");
                        }
                    });

                beanFactory.registerSingleton(className, controller);
                LOG.info("Dynamic resource registered: %s", className);
            }
        }
    }

}
