package dev.soffa.foundation.data.spring;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.annotation.Repository;
import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.commons.JavaUtil;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.EntityRepository;
import dev.soffa.foundation.data.SimpleDataStore;
import dev.soffa.foundation.data.SimpleEntityRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.MethodUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
class DynamicRepositoryBuilder {

    private static final Logger LOG = Logger.get(DynamicRepositoryBuilder.class);

    private final ApplicationContext context;
    private final String basePackage;
    private final DB db;

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    void register() {
        if (db == null) {
            LOG.warn("No database specified, skipping repository registration");
            return;
        }

        Set<Class<?>> resources = ClassUtil.findInterfacesAnnotatedWith(
            basePackage, Repository.class, EntityRepository.class
        );

        if (CollectionUtil.isEmpty(resources)) {
            LOG.info("No annotated @Repository found, skipping registration");
            return;
        }

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.context;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();

        SimpleDataStore sds = new SimpleDataStore(db);
        for (Class<?> resourceClass : resources) {

            LOG.debug("Registering repository: %s", resourceClass.getName());

            Repository res = resourceClass.getAnnotation(Repository.class);
            String collection = res.collection();
            String tenant = res.fixedTenant();
            Class<?>[] parameterizedTypes = ClassUtil.lookupGeneric(resourceClass, EntityRepository.class);
            Preconditions.checkArgument(parameterizedTypes != null && parameterizedTypes.length == 1, "Entity class not found for repository %s", resourceClass);
            SimpleEntityRepository<?> repo = new SimpleEntityRepository<>(sds, parameterizedTypes[0], collection, tenant);

            AtomicReference<MethodHandles.Lookup> instance = new AtomicReference<>(null);
            if (JavaUtil.isJava8()) {
                Constructor<Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                instance.set(constructor.newInstance(resourceClass, MethodHandles.Lookup.PRIVATE));
            }

            Object repositoryImpl = Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{resourceClass}, (proxy, method, args) -> {
                    if (method.isDefault()) {
                        if (instance.get() == null) {
                            return MethodHandles.lookup()
                                .findSpecial(
                                    resourceClass,
                                    method.getName(),
                                    MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                                    resourceClass)
                                .bindTo(proxy)
                                .invokeWithArguments(args);
                        }
                        return instance.get().unreflectSpecial(method, resourceClass).
                            bindTo(proxy).
                            invokeWithArguments(args);
                    } else {
                        return MethodUtils.invokeMethod(repo, method.getName(), args);
                    }
                });
            String beanName = resourceClass.getName() + "Impl";
            beanFactory.registerSingleton(beanName, repositoryImpl);
        }


    }

}
