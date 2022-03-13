package dev.soffa.foundation.pubsub.nats;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.message.Message;
import io.nats.client.Connection;
import io.nats.client.Subscription;
import io.nats.client.impl.NatsMessage;
import lombok.SneakyThrows;

import java.util.List;

public final class NatsUtil {

    private NatsUtil() {
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    static void unsubscribe(List<Subscription> subscriptions) {
        if (subscriptions == null) {
            return;
        }
        for (Subscription subscription : subscriptions) {
            try {
                subscription.unsubscribe();
            } catch (Exception ignore) {

                // Ignore any
            }
        }
    }

    @SuppressWarnings({"PMD.EmptyCatchBlock", "PMD.CloseResource"})
    static void close(Connection... connections) {
        if (connections == null) {
            return;
        }
        for (Connection connection : connections) {
            try {
                connection.close();
            } catch (Exception e) {
                // Ignore any
            }
        }
    }

    @SneakyThrows
    static NatsMessage createNatsMessage(String subject, Message message) {
        byte[] data = Mappers.JSON.serializeAsBytes(message);
        return new NatsMessage(
            subject,
            "",
            data
        );
    }

}
