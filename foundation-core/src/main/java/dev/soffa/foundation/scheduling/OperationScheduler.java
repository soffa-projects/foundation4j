package dev.soffa.foundation.scheduling;

import dev.soffa.foundation.commons.DigestUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.core.model.Serialized;

import java.util.UUID;

public interface OperationScheduler {

    default <I, O, T extends Operation<I, O>> void enqueue(Class<T> operationClass, I input) {
        enqueue(UUID.randomUUID(), operationClass, input, ContextHolder.inheritOrCreate());
    }

    default <I, O, T extends Operation<I, O>> void enqueue(String uuid, Class<T> operationClass, I input, Context context) {
        enqueue(DigestUtil.makeUUID(uuid), operationClass, input, context);
    }

    default <I, O, T extends Operation<I, O>> void enqueue(UUID uuid, Class<T> operationClass, I input, Context context) {
        enqueue(uuid, operationClass.getName(), Serialized.of(input), context);
    }

    void enqueue(UUID uuid, String operationName, Serialized serializedInput, Context context);

    default void enqueue(String uuid, String operationName, Serialized input, Context context) {
        enqueue(DigestUtil.makeUUID(uuid), operationName, input, context);
    }

    void scheduleRecurrently(String cronId, String cron, ServiceWorker worker);

}
