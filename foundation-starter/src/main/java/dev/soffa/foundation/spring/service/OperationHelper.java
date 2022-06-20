package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.annotation.DefaultTenant;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.multitenancy.TenantHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OperationHelper {

    private static final Map<String,Boolean> DEFAULTS = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.getLogger(OperationHelper.class);

    private OperationHelper() {
    }
    public static <I, O, T extends Operation<I, O>> O invoke(T operation, I input, Context ctx) {
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

    private static <I, O, T extends Operation<I, O>> O apply(Operation<I, O> operation, I input, @NonNull Context ctx) {
        LOG.info("Invoking operation %s with tenant %s", operation.getClass().getName(), ctx.getTenant());
        operation.validate(input, ctx);
        O res = operation.handle(input, ctx);
        operation.postHandle(res, ctx);
        return res;
    }
}
