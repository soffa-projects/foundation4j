package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.annotation.DefaultTenant;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Sentry;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Command;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.hooks.HookService;
import dev.soffa.foundation.hooks.action.ProcessHook;
import dev.soffa.foundation.hooks.action.ProcessHookItem;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class OperationDispatcher implements Dispatcher {

    private final OperationsMapping operations;
    private final ActivityService activities;
    private final HookService hooks;
    private final PubSubClient pubSubClient;
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

    @Override
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

    @Transactional
    protected <I, O, T extends Operation<I, O>> O apply(T operation, I input, @NonNull Context ctx) {
        String operationName = operations.getOperationId(operation);
        LOG.info("Invoking operation %s with tenant %s", operationName, ctx.getTenant());

        operation.validate(input, ctx);
        O res = operation.handle(input, ctx);

        if (operation instanceof Command) {
            activities.record(ctx, operationName, input);
            try {
                String pubSubOperation = ctx.getServiceName() + "." + TextUtil.snakeCase(operationName) + ".success";
                pubSubClient.broadcast(MessageFactory.create(pubSubOperation, res));
                LOG.info("Operation success event published: %s", pubSubOperation);
            } catch (Exception e) {
                Sentry.getInstance().captureException(e);
                LOG.error("Failed to publish operation success", e);
            }
        }

        return res;
    }
}
