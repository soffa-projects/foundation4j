package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.annotation.DefaultTenant;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Sentry;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Broadcast;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.core.Recorded;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.resource.Resource;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class OperationDispatcher implements Dispatcher, Resource {

    private final OperationsMapping operations;
    private final ActivityService activities;
    private final PubSubClient pubSubClient;
    private static final Map<String, Boolean> DEFAULT_TENANT = new ConcurrentHashMap<>();


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

        String className = operation.getClass().getSimpleName();

        if (!DEFAULT_TENANT.containsKey(className)) {
            DEFAULT_TENANT.put(className, AnnotationUtils.findAnnotation(operation.getClass(), DefaultTenant.class) != null);
        }
        if (DEFAULT_TENANT.get(className)) {
            return TenantHolder.useDefault(() -> apply(operation, input, ctx));
        } else {
            TenantId tenant = ctx.getTenant();
            TenantId override = operation.getTenant(input, ctx);
            if (override == null || override == TenantId.CONTEXT) {
                override = operation.getTenant(ctx);
            }
            if (override != TenantId.CONTEXT && override!=null) {
                tenant = override;
                Logger.platform.info("Token overriden for operation %s: %s", className, tenant);
            }
            return TenantHolder.use(tenant, () -> apply(operation, input, ctx));
        }
    }

    @Transactional
    protected <I, O, T extends Operation<I, O>> O apply(T operation, I input, @NonNull Context ctx) {
        String operationName = operations.getOperationId(operation);
        //return Sentry.get().watch("Operation dispatch: " + operationName, () -> {

        operation.validate(input, ctx);
        O res = operation.handle(input, ctx);

        if (operation instanceof Recorded) {
            activities.record(ctx, operationName, input);
        }

        if (operation instanceof Broadcast) {
            Sentry.get().watch("Operation success broadcast: " + operationName, () -> {
                String pubSubOperation = ctx.getServiceName() + "." + TextUtil.snakeCase(operationName) + ".success";
                pubSubClient.broadcast(MessageFactory.create(pubSubOperation, res));
            });
        }

        return res;

        //});
    }

    @Override
    public <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input, @NonNull Context ctx) {
        return dispatch(operationClass, input, ctx);
    }

    @Override
    public <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input) {
        return dispatch(operationClass, input, ContextHolder.require());
    }
}
