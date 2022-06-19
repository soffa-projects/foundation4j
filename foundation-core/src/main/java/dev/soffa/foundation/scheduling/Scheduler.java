package dev.soffa.foundation.scheduling;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;

public interface Scheduler {

    default <I, O, T extends Operation<I, O>> void enqueue(Class<T> operationClass, I input) {
        enqueue(operationClass, input, Context.create());
    }
    <I, O, T extends Operation<I, O>> void enqueue(Class<T> operationClass, I input, Context context);

}
