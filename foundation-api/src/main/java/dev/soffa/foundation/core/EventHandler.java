package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface EventHandler<I> {

    void handle(I input, @NonNull Context ctx);

}
