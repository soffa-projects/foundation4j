package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

public class NoopPubSubClient implements PubSubClient {


    @Override
    public <T> CompletableFuture<T> request(@NonNull String subject, Message message, Class<T> returnType) {
        return null;
    }

    @Override
    public void publish(@NonNull String subject, Message message) {
        // Nothing to do
    }

    @Override
    public void broadcast(@NonNull String target, Message message) {
        // Nothing to do
    }

    @Override
    public void broadcast(Message message) {
        // Nothing to do
    }

    @Override
    public void addBroadcastChannel(String value) {
        // Nothing to do
    }
}
