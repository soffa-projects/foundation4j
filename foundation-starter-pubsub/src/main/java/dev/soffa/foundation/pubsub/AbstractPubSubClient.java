package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.*;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageResponse;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.message.pubsub.PubSubClientConfig;
import dev.soffa.foundation.model.ResponseStatus;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractPubSubClient implements PubSubClient {

    private final Set<String> subscritpions = new HashSet<>();
    protected Set<String> broadcasting = new HashSet<>();
    protected String applicationName;

    public AbstractPubSubClient(String applicationName, PubSubClientConfig config, String broadcasting) {
        this.applicationName = applicationName;
        if (config != null && TextUtil.isNotEmpty(config.getBroadcasting())) {
            this.broadcasting.add(config.getBroadcasting());
        }
        if (TextUtil.isNotEmpty(broadcasting)) {
            this.broadcasting.add(broadcasting);
        }
    }

    public boolean hasSubscription(String name) {
        return subscritpions.contains(name.toLowerCase());
    }

    public void registerSubscription(String name) {
        if (subscritpions.contains(name.toLowerCase())) {
            throw new PubSubException("A subscription already exists for: " + name);
        }
        subscritpions.add(name.toLowerCase());
    }

    @Override
    public void addBroadcastChannel(String value) {
        if (TextUtil.isNotEmpty(value)) {
            this.broadcasting.add(value);
        }
    }

    @Override
    final public void broadcast(Message message) {
        broadcasting.forEach(s -> publish(s, message));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> CompletableFuture<T> request(@NonNull String subject, Message message, final Class<T> returnType) {
        return sendAndReceive(subject, message).thenApply(data -> unwrapResponse(data, returnType));
    }

    protected abstract CompletableFuture<byte[]> sendAndReceive(@NonNull String subject, Message message);

    public <T> T unwrapResponse(byte[] data, final Class<T> responseClass) {
        if (data == null) {
            return null;
        }
        MessageResponse response = Mappers.JSON_DEFAULT.deserialize(data, MessageResponse.class);
        if (response.isSuccess()) {
            return Mappers.JSON_DEFAULT.deserialize(response.getData(), responseClass);
        } else {
            switch (response.getErrorCode()) {
                case ResponseStatus.UNAUTHORIZED:
                    throw new UnauthorizedException(response.getError());
                case ResponseStatus.FORBIDDEN:
                    throw new ForbiddenException(response.getError());
                case ResponseStatus.BAD_REQUEST:
                    throw new FunctionalException(response.getError());
                default:
                    throw new TechnicalException(response.getError());
            }
        }
    }
}
