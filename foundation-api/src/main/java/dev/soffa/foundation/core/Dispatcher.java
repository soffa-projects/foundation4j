package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.resource.Resource;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Dispatcher extends Resource {

    <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input, Context ctx);

    <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input);

    <I, O, T extends Operation<I, O>> O invoke(T operation, I input, Context ctx);

    @Override
    default <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input, @NonNull Context ctx) {
        return dispatch(operationClass, input, ctx);
    }

    @Override
    default <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input) {
        return dispatch(operationClass, input);
    }

}
