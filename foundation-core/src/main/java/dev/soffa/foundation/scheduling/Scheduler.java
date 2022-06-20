package dev.soffa.foundation.scheduling;

import dev.soffa.foundation.commons.DigestUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;

import java.util.UUID;

public interface Scheduler {

    default <I, O, T extends Operation<I, O>> void enqueue(Class<T> operationClass, I input) {
        enqueue(operationClass, input, Context.create());
    }

    <I, O, T extends Operation<I, O>> void enqueue(Class<T> operationClass, I input, Context context);

    default <I, O, T extends Operation<I, O>> void enqueue(String uuid, Class<T> operationClass, I input, Context context) {
        enqueue(DigestUtil.makeUUID(uuid), operationClass, input, context);
    }

    <I, O, T extends Operation<I, O>> void enqueue(UUID uuid, Class<T> operationClass, I input, Context context);

}
