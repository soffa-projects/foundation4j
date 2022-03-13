package dev.soffa.foundation.data.spring;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.annotations.Repository;
import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.EntityRepository;
import dev.soffa.foundation.data.SimpleDataStore;
import dev.soffa.foundation.data.SimpleEntityRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.MethodUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

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
            return;
        }
        Reflections reflections = new Reflections(basePackage);

        //EL
        Set<Class<?>> resources = reflections.get(
            SubTypes.of(TypesAnnotated.with(Repository.class)).asClass().filter(EntityRepository.class::isAssignableFrom)
        );

        if (CollectionUtil.isEmpty(resources)) {
            return;
        }

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.context;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();

        SimpleDataStore sds = new SimpleDataStore(db);
        for (Class<?> resourceClass : resources) {
            Repository res = resourceClass.getAnnotation(Repository.class);
            String collection = res.collection();
            String tenant = res.fixedTenant();
            Class<?>[] parameterizedTypes = ClassUtil.lookupGeneric(resourceClass, EntityRepository.class);
            Preconditions.checkArgument(parameterizedTypes!=null && parameterizedTypes.length == 1, "Entity class not found for repository %s", resourceClass);
            SimpleEntityRepository<?> repo = new SimpleEntityRepository<>(sds, parameterizedTypes[0], collection, tenant);

            Constructor<Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            MethodHandles.Lookup instance = constructor.newInstance(resourceClass, MethodHandles.Lookup.PRIVATE);

            Object repositoryImpl = Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{resourceClass}, (proxy, method, args) -> {
                    if (method.isDefault()) {
                        return instance.unreflectSpecial(method, resourceClass).
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
