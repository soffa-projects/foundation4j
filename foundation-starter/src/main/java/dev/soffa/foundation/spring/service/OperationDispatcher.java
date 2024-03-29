package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.annotation.DefaultTenant;
import dev.soffa.foundation.commons.DefaultIdGenerator;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.context.DefaultOperationContext;
import dev.soffa.foundation.core.*;
import dev.soffa.foundation.core.model.Serialized;
import dev.soffa.foundation.error.ForbiddenException;
import dev.soffa.foundation.events.OnServiceStarted;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.resource.Resource;
import dev.soffa.foundation.scheduling.ServiceWorker;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
@AllArgsConstructor
public class OperationDispatcher implements Dispatcher, Resource {

    private static final Map<String, Boolean> DEFAULT_TENANT_OPERATIONS = new ConcurrentHashMap<>();
    private final ApplicationContext context;
    private final SideEffectsHandler sideEffectsHandler;
    private final AtomicReference<OperationsMapping> operationsMapping = new AtomicReference<>(null);

    private OperationsMapping getOperations() {
        if (operationsMapping.get() == null) {
            operationsMapping.set(context.getBean(OperationsMapping.class));
        }
        return operationsMapping.get();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O dispatch(String operationName, Serialized input, String serializedContext) {
        Logger.platform.info("Dispatching operation: %s", operationName);
        try {
            Operation<I, O> op = getOperations().require(operationName);
            I deserialized = null;
            if (input.getData() != null) {
                deserialized = (I) Mappers.JSON_DEFAULT.deserialize(input.getData(), Class.forName(input.getType()));
            }
            Context context = Mappers.JSON_DEFAULT.deserialize(serializedContext, Context.class);
            return invoke(op, deserialized, context);
        } catch (Exception e) {
            Logger.platform.error("Error while dispatching operation: %s", operationName, e);
            throw e;
        }
    }

    @Override
    public <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input, Context ctx) {
        return invoke(getOperations().require(operationClass), input, ctx);
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
        if (ctx == null) {
            throw new ForbiddenException("No context provided");
        }

        String className = operation.getClass().getSimpleName();

        Logger.platform.debug("Invoking operation %s [livemode=%s]", className, ctx.isLiveMode());

        if (!DEFAULT_TENANT_OPERATIONS.containsKey(className)) {
            DEFAULT_TENANT_OPERATIONS.put(className, AnnotationUtils.findAnnotation(operation.getClass(), DefaultTenant.class) != null);
        }


        if (DEFAULT_TENANT_OPERATIONS.get(className)) {
            return TenantHolder.useDefault(() -> apply(operation, input, ctx));
        } else {
            TenantId tenant = ctx.getTenant();
            if (tenant == null) {
                tenant = TenantId.DEFAULT;
            }
            TenantId override = operation.getTenant(input, ctx);
            if (override == null || TenantId.CONTEXT.equals(override)) {
                override = operation.getTenant(ctx);
            }
            if (override != null && !TenantId.CONTEXT.equals(override) && !tenant.equals(override)) {
                tenant = override;
                Logger.platform.debug("Tenant overriden for operation %s: %s", className, tenant);
            }
            return TenantHolder.use(tenant, () -> apply(operation, input, ctx));
        }

    }

    @SneakyThrows
    @Transactional
    protected <I, O, T extends Operation<I, O>> O apply(T operation, I input, @NonNull Context ctx) {
        String operationName = getOperations().getOperationId(operation);
        DefaultOperationContext opContext = new DefaultOperationContext(ctx, operation.getClass());
        operation.validate(input, ctx);
        O res = operation.handle(input, opContext);

        if (operation instanceof Recorded) {
            opContext.activity(operationName, null, input);
        }

        /*if (operation instanceof Broadcast) {
            String pubSubOperation = ctx.getServiceName() + "." + TextUtil.snakeCase(operationName) + ".success";
            opContext.delayed(
                DefaultIdGenerator.uuid(operationName),
                PublishEvent.class,
                new Event(pubSubOperation, Mappers.JSON_DEFAULT.serialize(res))
            );
        }*/

        boolean shouldEnqueue = operation instanceof Command && !(operation instanceof ServiceWorker) &&
            !opContext.getSideEffects().isEmpty() && !(operation instanceof OnServiceStarted);

        if (shouldEnqueue) {
            sideEffectsHandler.enqueue(operationName,
                DefaultIdGenerator.uuid(TextUtil.snakeCase(operationName) + "_"),
                opContext.getSideEffects(),
                ctx
            );
        }
        return res;
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
