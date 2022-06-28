package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.model.TenantId;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Operation<I, O> extends TenantDelegate {

    Void NO_ARG = null;

    O handle(I input, @NonNull Context ctx);

    default TenantId getTenant(I input, Context context) {
        return TenantId.CONTEXT;
    }

    default void validate(I input, @NonNull Context ctx) {
        // Nothing to do here
    }

}
