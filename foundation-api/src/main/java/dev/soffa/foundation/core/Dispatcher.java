package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;

public interface Dispatcher {

    <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input, Context ctx);

    <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input);

    <I, O, T extends Operation<I, O>> O invoke(T operation, I input, Context ctx);


}
