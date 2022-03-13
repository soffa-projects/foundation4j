package dev.soffa.foundation.resource;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Resource {

    <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input, @NonNull Context ctx);

    <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input);

}
