package dev.soffa.foundation.message.pubsub;


import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.core.RemoteOperation;
import dev.soffa.foundation.error.TodoException;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.MessageHandler;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

public interface PubSubClient {

    @SneakyThrows
    default void subscribe(@NonNull String subject, MessageHandler messageHandler) {
        subscribe(subject, false, messageHandler);
    }

    @SneakyThrows
    default void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {
        throw new TodoException("Not implemented");
    }

    default void subscribe(MessageHandler messageHandler) {
        throw new TodoException("Not implemented");
    }

    <T> CompletableFuture<T> request(@NonNull String subject, @NotNull Message message, Class<T> returnType);

    default <I, O, T extends Operation<I, O>> CompletableFuture<O> request(String target, Class<T> operation, I input, Class<O> returnType) {
        return request(target, MessageFactory.create(operation.getSimpleName(), input), returnType);
    }

    default <I, O, T extends Operation<I, O>> CompletableFuture<O> request(String target, Class<T> operation, Class<O> returnType) {
        return request(target, MessageFactory.create(operation.getSimpleName(), null), returnType);
    }

    void publish(@NonNull String subject, @NotNull Message message);

    default void publish(@NotNull Message message) {
        throw new TodoException("Not implemented");
    }

    @SneakyThrows
    void broadcast(@NonNull String target, @NotNull Message message);

    void broadcast(@NotNull Message message);

    default void broadcast(String operation, Object payload) {
        broadcast(MessageFactory.create(operation, payload));
    }

    // <I, O, T extends Query<I, O>> T proxy(@NonNull String subjet, @NotNull Class<T> operationClass);

    void addBroadcastChannel(String value);

    default <I, O, T extends Operation<I, O>> RemoteOperation<I,O> createOperationCaller(@NotNull Class<T> operationClass, @NonNull String channel) {
        return PubSubClientFactory.of(this, operationClass, channel);
    }

    /*
    @SuppressWarnings("unchecked")
    default <T> T createClient(Class<T> clientInterface, String subject) {

        Map<Method, String> mapping = new HashMap<>();

        for (Method method : clientInterface.getDeclaredMethods()) {
            BindOperation binding = method.getAnnotation(BindOperation.class);
            if (binding != null) {
                mapping.put(method, binding.value().getName());
            }
        }

        if (mapping.isEmpty()) {
            throw new TechnicalException("No method found with annotation @BindOperation");
        }

        return (T) java.lang.reflect.Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{clientInterface},
            (proxy, method, args) -> {
                if ("hashCode".equals(method.getName())) {
                    return clientInterface.getName().hashCode();
                }
                if ("equals".equals(method.getName())) {
                    return method.equals(args[0]);
                }
                if (!mapping.containsKey(method)) {
                    throw new TechnicalException("This method has no @BindOperation annotation");
                }
                return request(subject, createMessage(mapping.get(method), args), method.getReturnType()).get(30, TimeUnit.SECONDS);
            });
    }

    */

}


