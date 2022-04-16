package dev.soffa.foundation.spring.aop;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.ValidationUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextUtil;
import dev.soffa.foundation.error.ErrorUtil;
import dev.soffa.foundation.error.ManagedException;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.error.UnauthorizedException;
import dev.soffa.foundation.extra.audit.AuditService;
import dev.soffa.foundation.metric.MetricsRegistry;
import dev.soffa.foundation.model.Validatable;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static dev.soffa.foundation.metric.CoreMetrics.OPERATION_PREFIX;

@Aspect
@Component
public class OperationHandlerAspect {

    private final MetricsRegistry metricsRegistry;
    private final AuditService auditService;

    public OperationHandlerAspect(MetricsRegistry metricsRegistry, @Autowired(required = false) AuditService auditService) {
        this.metricsRegistry = metricsRegistry;
        this.auditService = auditService;
    }

    @SneakyThrows
    @Around("execution(* dev.soffa.foundation.core.Operation.*(..))")
    public Object handleOperation(ProceedingJoinPoint jp) {
        Object[] args = jp.getArgs();

        AtomicReference<Object> input = new AtomicReference<>(null);
        Context context;
        boolean hasInput = args.length == 2;

        if (hasInput) {
            input.set(args[0]);
            context = (Context) args[1];
            if (input.get() != null) {
                if (input.get() instanceof Validatable) {
                    ((Validatable) input.get()).validate();
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
        Map<String, Object> tags = ContextUtil.tagify(context);

        return Logger.withContext(ImmutableMap.of("operation", operationId), () -> {
            //noinspection Convert2Lambda
            return metricsRegistry.track(OPERATION_PREFIX + operationId, tags, new Supplier<Object>() {
                @Override
                public Object get() {
                    return doProceed(jp, operationId, input.get(), args, context);
                }
            });
        });
    }

    @SneakyThrows
    private Object doProceed(ProceedingJoinPoint jp, String operationId, Object input, Object[] args, Context context) {
        try {
            Object result = jp.proceed(args);
            if (auditService != null) {
                auditService.log(operationId, input, result, context);
            }
            return result;
        } catch (AuthenticationCredentialsNotFoundException e) {
            if (auditService != null) {
                auditService.log(operationId, input, null, e, context);
            }
            throw new UnauthorizedException(e.getMessage(), ErrorUtil.getStacktrace(e));
        } catch (Exception e) {
            if (auditService != null) {
                auditService.log(operationId, input, null, e, context);
            }
            if (e instanceof ManagedException) {
                throw e;
            }
            throw new TechnicalException(e);
        }
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
