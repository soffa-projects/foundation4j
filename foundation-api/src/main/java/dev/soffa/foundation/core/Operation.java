package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Operation<I, O> {

    Void NO_ARG = null;

    O handle(I input, @NonNull Context ctx);

    default O handle(@NonNull Context ctx) {
        return handle(null, ctx);
    }

}
