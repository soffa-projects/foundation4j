package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.annotation.DefaultTenant;
import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.context.DefaultOperationContext;
import dev.soffa.foundation.core.*;
import dev.soffa.foundation.core.action.PublishEvent;
import dev.soffa.foundation.core.model.Serialized;
import dev.soffa.foundation.model.Event;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.resource.Resource;
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

    private final ApplicationContext context;
    private final SideEffectsHandler sideEffectsHandler;
    private static final Map<String, Boolean> DEFAULT_TENANT = new ConcurrentHashMap<>();
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
        Operation<I, O> op = getOperations().require(operationName);
        I deserialized = (I) Mappers.JSON_FULLACCESS.deserialize(input.getData(), Class.forName(input.getType()));
        Context context = Mappers.JSON_FULLACCESS.deserialize(serializedContext, Context.class);
        return invoke(op, deserialized, context);
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


        String className = operation.getClass().getSimpleName();

        Logger.app.debug("Invoking operation %s", className);

        if (!DEFAULT_TENANT.containsKey(className)) {
            DEFAULT_TENANT.put(className, AnnotationUtils.findAnnotation(operation.getClass(), DefaultTenant.class) != null);
        }

        if (DEFAULT_TENANT.get(className)) {
            return TenantHolder.useDefault(() -> apply(operation, input, ctx));
        } else {
            TenantId tenant = ctx.getTenant();
            TenantId override = operation.getTenant(input, ctx);
            if (override == null || TenantId.CONTEXT.equals(override)) {
                override = operation.getTenant(ctx);
            }
            if (!TenantId.CONTEXT.equals(override) && override != null) {
                tenant = override;
                Logger.platform.info("Token overriden for operation %s: %s", className, tenant);
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

        if (operation instanceof Broadcast) {
            String pubSubOperation = ctx.getServiceName() + "." + TextUtil.snakeCase(operationName) + ".success";
            opContext.delayed(
                DefaultIdGenerator.uuid(operationName),
                PublishEvent.class,
                new Event(pubSubOperation, Mappers.JSON.serialize(res))
            );
        }
        if (!opContext.getSideEffects().isEmpty()) {
            sideEffectsHandler.enqueue(operationName,
                DigestUtil.md5(Mappers.JSON.serialize(input)),
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
