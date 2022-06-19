package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.transaction.Transactional;

public interface Operation<I, O> {

    Void NO_ARG = null;



    @Transactional
    O handle(I input, @NonNull Context ctx);

    default O handle(@NonNull Context ctx) {
        return handle(null, ctx);
    }

    default void postHandle(O result, @NonNull Context ctx) {
        // do nothing
    }
    default void validate(I input, @NonNull Context ctx) {
        // do nothing
    }

}
