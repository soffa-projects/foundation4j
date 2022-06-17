package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.ForbiddenException;
import dev.soffa.foundation.error.FunctionalException;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.error.UnauthorizedException;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageResponse;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.message.pubsub.PubSubClientConfig;
import dev.soffa.foundation.model.ResponseStatus;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractPubSubClient implements PubSubClient {

    private static final Logger LOG = Logger.get(PubSubClient.class);
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
    public final <T> CompletableFuture<T> request(@NonNull String subject, Message message, final Class<T> responseClass) {
        return sendAndReceive(subject, message).thenApply(data -> unwrapResponse(data, responseClass));
    }

    protected abstract CompletableFuture<byte[]> sendAndReceive(@NonNull String subject, Message message);

    public <T> T unwrapResponse(byte[] data, final Class<T> responseClass) {
        if (data == null) {
            return null;
        }
        MessageResponse response = Mappers.JSON.deserialize(data, MessageResponse.class);
        if (response.isSuccess()) {
            return Mappers.JSON.deserialize(response.getData(), responseClass);
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
