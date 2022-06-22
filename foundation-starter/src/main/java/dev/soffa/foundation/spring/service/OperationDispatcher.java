package dev.soffa.foundation.spring.service;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.annotation.DefaultTenant;
import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.hooks.action.ProcessHook;
import dev.soffa.foundation.hooks.action.ProcessHookInput;
import dev.soffa.foundation.model.EventModel;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.scheduling.Scheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OperationDispatcher implements Dispatcher {

    private final OperationsMapping operations;
    private final ApplicationContext context;
    private Scheduler scheduler;

    public OperationDispatcher(OperationsMapping operations, ApplicationContext context) {
        this.operations = operations;
        this.context = context;
    }

    private static final Map<String, Boolean> DEFAULTS = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.getLogger(OperationInvoker.class);


    @Override
    public <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input, Context ctx) {
        return invoke(operations.require(operationClass), input, ctx);
    }

    @Override
    public <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input) {
        return dispatch(operationClass, input, ContextHolder.require());
    }

    public <I, O, T extends Operation<I, O>> O invoke(T operation, I input, Context ctx) {
        if (operation == null) {
            return null;
        }

        String className = operation.getClass().getName();
        if (!DEFAULTS.containsKey(className)) {
            DEFAULTS.put(className, AnnotationUtils.findAnnotation(operation.getClass(), DefaultTenant.class) != null);
        }
        if (DEFAULTS.get(className)) {
            return TenantHolder.useDefault(() -> apply(operation, input, ctx));
        } else {
            return TenantHolder.use(ctx.getTenant(), () -> apply(operation, input, ctx));
        }
    }

    private <I, O, T extends Operation<I, O>> O apply(T operation, I input, @NonNull Context ctx) {
        String operationName = AopUtils.getTargetClass(operation).getSimpleName();
        String operationId = TextUtil.snakeCase(operationName);
        LOG.info("Invoking operation %s with tenant %s", operationName, ctx.getTenant());

        if (scheduler == null) {
            scheduler = context.getBean(Scheduler.class);
            Preconditions.checkNotNull(scheduler, "A scheduler is required in the current context");
        }

        try {
            operation.validate(input, ctx);
            O res = operation.handle(input, ctx);
            operation.postHandle(res, ctx);
            String messageId = null;
            if (res instanceof EventModel) {
                messageId = operationId + "." + ((EventModel) res).getId();
            }
            scheduler.enqueue(ProcessHook.class, new ProcessHookInput(operationId, messageId, Mappers.JSON.toMap(res)));
            Sentry.getInstance().captureEvent(ctx, operationId + ".success", messageId);
            return res;
        } catch (Exception e) {
            Sentry.getInstance().captureEvent(ctx, operationId + ".failed", EventLevel.ERROR);
            throw e;
        }

    }
}
