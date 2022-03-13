package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.annotations.Bind;
import dev.soffa.foundation.annotations.Resource;
import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.errors.ConfigurationException;
import dev.soffa.foundation.errors.TodoException;
import dev.soffa.foundation.http.MediaType;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.SuperMethodCall;
import org.apache.commons.beanutils.BeanUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import javax.validation.Valid;
import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

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

    private void configure(DefaultListableBeanFactory beanFactory) {
        Reflections reflections = new Reflections(appConfig.getPkg());
        Set<Class<?>> resources =
            reflections.get(SubTypes.of(TypesAnnotated.with(Resource.class)).asClass());

        if (CollectionUtil.isEmpty(resources)) {
            return;
        }

        LOG.info("Found %d resources", resources.size());

        for (Class<?> clazz : resources) {
            String className = clazz.getName() + "Impl";
            if (!LOADED.contains(className)) {
                LOADED.add(className);
                Class<?> impleClazz = createDynamicController(clazz, className);
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(impleClazz);
                beanFactory.registerBeanDefinition(className, beanDefinitionBuilder.getBeanDefinition());
            }
        }

    }

    @SneakyThrows
    private Class<?> createDynamicController(Class<?> interfaceClass, String className) {
        LOG.info("Creating dynamic controller for %s", interfaceClass.getName());
        String basePath = "/";
        Path pathAnno = interfaceClass.getAnnotation(Path.class);
        if (pathAnno != null) {
            basePath = pathAnno.value();
        }

        // Create class
        DynamicType.Builder<?> generator =
            new ByteBuddy()
                .with(TypeValidation.ENABLED)
                .subclass(interfaceClass)
                .annotateType(AnnotationDescription.Builder.ofType(RestController.class).build(),
                    AnnotationDescription.Builder.ofType(RequestMapping.class)
                        .defineArray("value", basePath)
                        .defineArray("produces", MediaType.APPLICATION_JSON_VALUE)
                        .build())
                .name(className);

        for (Method method : interfaceClass.getDeclaredMethods()) {

            if (method.getName().startsWith("$jacoco")) {
                continue;
            }

            DynamicType.Builder.MethodDefinition.ParameterDefinition<?> methodBuilder = generator.defineMethod(
                method.getName(),
                method.getReturnType(),
                method.getModifiers()
            );

            for (Parameter param : method.getParameters()) {
                List<AnnotationDescription> annotations = buildParamAnnotations(param);
                methodBuilder = methodBuilder.withParameter(param.getType(), param.getName(), param.getModifiers())
                    .annotateParameter(annotations.toArray(new AnnotationDescription[0]));
            }

            Implementation interceptor = createImplementation(interfaceClass, method);
            generator = methodBuilder
                .intercept(interceptor)
                .annotateMethod(createMethodAnnotation(method));

        }

        DynamicType.Unloaded<?> generatedClass = generator.make();
        LOG.info("Dynamic controller generated for %s", interfaceClass.getName());
        return generatedClass.load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
    }

    @NotNull
    private List<AnnotationDescription> buildParamAnnotations(Parameter param) {
        List<AnnotationDescription> annotations = new ArrayList<>();
        if (param.getType() == Context.class) {
            annotations.add(
                AnnotationDescription.Builder
                    .ofType(io.swagger.v3.oas.annotations.Parameter.class)
                    .define("hidden", true).build()
            );
        } else if (param.isAnnotationPresent(RequestBody.class)) {
            annotations.add(AnnotationDescription.Builder.ofType(org.springframework.web.bind.annotation.RequestBody.class).build());
            annotations.add(AnnotationDescription.Builder.ofType(Valid.class).build());
        }

        return annotations;
    }

    private AnnotationDescription createMethodAnnotation(Method method) {

        List<RequestMethod> methods = new ArrayList<>();
        List<String> paths = new ArrayList<>();

        io.swagger.v3.oas.annotations.Operation oasOp = method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
        if (oasOp != null) {
            methods.add(RequestMethod.valueOf(oasOp.method().toUpperCase()));
        }
        if (method.isAnnotationPresent(POST.class)) {
            methods.add(RequestMethod.POST);
        }
        if (method.isAnnotationPresent(PUT.class)) {
            methods.add(RequestMethod.PUT);
        }
        if (method.isAnnotationPresent(GET.class)) {
            methods.add(RequestMethod.GET);
        }
        if (method.isAnnotationPresent(PATCH.class)) {
            methods.add(RequestMethod.PATCH);
        }
        if (method.isAnnotationPresent(DELETE.class)) {
            methods.add(RequestMethod.DELETE);
        }

        if (methods.isEmpty()) {
            LOG.info("No http method was found for operation: %s.%s, assuming GET", method.getDeclaringClass().getName(), method.getName());
            methods.add(RequestMethod.GET);
        }

        Path pathAnno = method.getAnnotation(Path.class);
        if (pathAnno != null) {
            paths.add(pathAnno.value());
        }

        if (paths.isEmpty()) {
            paths.add("");
        }

        return AnnotationDescription.Builder
            .ofType(RequestMapping.class)
            .defineArray("value", paths.toArray(new String[0]))
            .defineEnumerationArray("method", RequestMethod.class, methods.toArray(new RequestMethod[0]))
            .build();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @NotNull
    private Implementation createImplementation(Class<?> interfaceClass, Method method) {

        if (method.isDefault()) {

            return SuperMethodCall.INSTANCE;

        } else {


            String operationName;
            Bind binding = method.getAnnotation(Bind.class);
            if (binding != null) {
                operationName = binding.value().getSimpleName();
            } else {
                operationName = method.getName().substring(0, 1).toUpperCase() + method.getName().substring(1);
            }

            Operation<?, ?> operation = operationsMapping.lookup(operationName).orElseThrow(() -> {
                //EL
                return new TodoException(
                    "Unable to find operation binding for: %s.%s (check method name or use @Bind)", interfaceClass.getName(), method.getName()
                );
            });

            return InvocationHandlerAdapter.of((proxy, m, args) -> {
                if ("hashCode".equals(method.getName())) {
                    return m.getDeclaringClass().getName().hashCode();
                }
                if ("equals".equals(method.getName())) {
                    return m.getName().equals(method.getName());
                }

                Object input = null;
                if (args != null && args.length > 0) {
                    boolean hasOneItem = args.length == 1;
                    if (hasOneItem) {
                        input = args[0];
                    } else {
                        throw new ConfigurationException("A resource operation cannot declare more than on argument. Location %s.%s", method.getDeclaringClass().getName(), method.getName());
                    }
                }

                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    Map<String, ?> pathVariables = (Map<String, ?>) requestAttributes.getAttribute(
                        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                        RequestAttributes.SCOPE_REQUEST
                    );
                    if (CollectionUtil.isNotEmpty(pathVariables)) {
                        for (Map.Entry<String, ?> e : pathVariables.entrySet()) {
                            if (input == null || ClassUtil.isBaseType(input.getClass())) {
                                input = e.getValue();
                            } else {
                                BeanUtils.setProperty(input, e.getKey(), e.getValue());
                            }
                        }
                    }
                }
                return ((Operation<Object, Object>) operation).handle(input, ContextHolder.require());
            });
        }
    }


}
