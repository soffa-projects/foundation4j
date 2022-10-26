package dev.soffa.foundation.core;

import dev.soffa.foundation.commons.ExecutorHelper;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.model.Serialized;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Dispatcher extends BaseDispatcher {

    <I, O> O dispatch(String operationName, Serialized input, String serializedContext);

    <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input, Context ctx);
    default <I, O, T extends Operation<I, O>> void dispatchAsync(Class<T> operationClass, I input, Context ctx) {
        ExecutorHelper.execute(() -> dispatch(operationClass, input, ctx));
    }


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
