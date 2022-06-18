package dev.soffa.foundation.message;

import dev.soffa.foundation.commons.IdGenerator;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class MessageFactory {

    private MessageFactory() {
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T getPayload(final Message message) {
        if (message.getPayload() == null) {
            return null;
        }
        return (T) Mappers.JSON.deserialize(message.getPayload(), Class.forName(message.getPayloadType()));
    }

    @SneakyThrows
    public static <T> T getPayload(final Message message, Class<T> type) {
        if (type == Void.class) {
            return null;
        }
        return Mappers.JSON.deserialize(message.getPayload(), type);
    }

    public static Message create(String operation, Object payload) {
        byte[] lPayload = null;
        String payloadType = null;
        if (payload != null) {
            lPayload = Mappers.JSON.serialize(payload).getBytes(StandardCharsets.UTF_8);
            payloadType = payload.getClass().getName();
        }
        Context context = ContextHolder.inheritOrCreate();
        context.sync();
        Map<String, String> headers = context.getHeaders();
        return new Message(IdGenerator.uuid("msg"), operation, lPayload, payloadType, headers);
    }

    public static Message create(String operation) {
        return create(operation, null);
    }

}
