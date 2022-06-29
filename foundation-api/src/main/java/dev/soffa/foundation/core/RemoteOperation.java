package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface RemoteOperation<I, O> {

    O invoke(I input, @NonNull Context ctx);

}
