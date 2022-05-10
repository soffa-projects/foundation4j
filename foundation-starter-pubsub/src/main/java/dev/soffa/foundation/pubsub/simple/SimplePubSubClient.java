package dev.soffa.foundation.pubsub.simple;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.error.ConfigurationException;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageHandler;
import dev.soffa.foundation.message.MessageResponse;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.pubsub.AbstractPubSubClient;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SimplePubSubClient extends AbstractPubSubClient implements PubSubClient {

    private final Map<String, MessageHandler> subscriptions = new ConcurrentHashMap<>();


    public SimplePubSubClient() {
        super(null, null, null);
    }

    @Override
    public void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {
        subscriptions.putIfAbsent(subject, messageHandler);
    }

    @Override
    protected CompletableFuture<byte[]> sendAndReceive(@NonNull String subject, Message message) {
        checkSubject(subject);
        return CompletableFuture.supplyAsync(() -> {
            Object result = subscriptions.get(subject).handle(message).orElse(null);
            if (result == null) {
                return null;
            }
            MessageResponse opr = MessageResponse.of(result, null);
            return Mappers.JSON.serializeAsBytes(opr);
        });
    }

    @Override
    public void publish(@NonNull String subject, Message message) {
        checkSubject(subject);
        subscriptions.get(subject).handle(message);
    }

    @Override
    public void broadcast(@NonNull String target, Message message) {
        if ("*".equals(target)) {
            new HashSet<>(subscriptions.values()).forEach(handler -> handler.handle(message));
            return;
        }
        checkSubject(target);
        subscriptions.get(target).handle(message);
    }

    @Override
    public void setDefaultBroadcast(String value) {
        // no-op
    }

    private void checkSubject(String target) {
        if (!subscriptions.containsKey(target)) {
            throw new ConfigurationException("Unregistered subject: %s", target);
        }
    }
}
