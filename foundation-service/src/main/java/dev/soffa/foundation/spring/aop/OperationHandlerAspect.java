package dev.soffa.foundation.spring.aop;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.ValidationUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.RequestContextUtil;
import dev.soffa.foundation.errors.ErrorUtil;
import dev.soffa.foundation.errors.ManagedException;
import dev.soffa.foundation.errors.TechnicalException;
import dev.soffa.foundation.errors.UnauthorizedException;
import dev.soffa.foundation.metrics.MetricsRegistry;
import dev.soffa.foundation.models.Validatable;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.Map;

import static dev.soffa.foundation.CoreMetrics.OPERATION_PREFIX;

@Aspect
@Component
@AllArgsConstructor
public class OperationHandlerAspect {

    private final MetricsRegistry metricsRegistry;

    @SneakyThrows
    @Around("execution(* dev.soffa.foundation.core.Operation.*(..))")
    public Object handleOperation(ProceedingJoinPoint jp) {
        Object[] args = jp.getArgs();

        Object input;
        Context context;
        boolean hasInput = args.length == 2;

        if (hasInput) {
            input = args[0];
            context = (Context) args[1];
            if (input!=null) {
                if (input instanceof Validatable) {
                    ((Validatable) input).validate();
                } else {
                    //TODO: use caching here
                    MethodSignature signature = (MethodSignature) jp.getSignature();
                    Method method = signature.getMethod();
                    if (method.getParameters()[0].getAnnotation(Valid.class) != null) {
                        ValidationUtil.validate(input);
                    }
                }
            }
        } else {
            context = (Context) args[0];
        }

        String operationId = jp.getTarget().getClass().getSimpleName();
        Map<String, Object> tags = RequestContextUtil.tagify(context);

        return Logger.withContext(ImmutableMap.of("operation", operationId), () -> {
            //noinspection Convert2Lambda
            return metricsRegistry.track(OPERATION_PREFIX + operationId, tags, new Supplier<Object>() {
                @SneakyThrows
                @Override
                public Object get() {
                    try {
                        return jp.proceed(args);
                    } catch (AuthenticationCredentialsNotFoundException e) {
                        throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
                    } catch (Exception e) {
                        if (e instanceof ManagedException) {
                            throw e;
                        }
                        throw new TechnicalException(e);
                    }
                }
            });
        });
    }


   /*
    @Override
    public <O> O handle(Class<? extends NoInputOperation<O>> operationClass) {
        RequestContext context = RequestContextHolder.require();
        return handle(operationClass, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public <O> O handle(Class<? extends NoInputOperation<O>> operationClass, RequestContext context) {

        if (SecurityContextHolder.getContext().getAuthentication() == null && context.hasAuthorization()) {
            authManager.process(context);
        }

        for (NoInputOperation<?> act : mapping.getRegistry0()) {
            if (operationClass.isAssignableFrom(act.getClass())) {
                NoInputOperation<O> impl = (NoInputOperation<O>) act;
                return metricsRegistry.track(OPERATION_PREFIX + operationClass.getName(), ImmutableMap.of(
                    Constants.OPERATION, operationClass.getName()
                ), () -> {
                    try {
                        return impl.handle(context);
                    } catch (AuthenticationCredentialsNotFoundException e) {
                        throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
                    }
                });
            }
        }
        metricsRegistry.increment(CoreMetrics.INVALID_OPERATION);
        throw new TechnicalException("Unable to find implementation for operation: %s", operationClass.getName());
    }
    */
}
