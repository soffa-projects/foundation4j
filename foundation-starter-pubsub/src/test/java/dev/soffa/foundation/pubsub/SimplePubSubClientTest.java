package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.messages.Message;
import dev.soffa.foundation.messages.pubsub.PubSubClient;
import dev.soffa.foundation.pubsub.simple.SimplePubSubClient;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePubSubClientTest {

    @Test
    void testClient() {

        PubSubClient client = new SimplePubSubClient();
        AtomicLong counter = new AtomicLong(0);
        client.subscribe("test", false, message -> {
            counter.incrementAndGet();
            return Optional.empty();
        });

        client.publish("test", new Message("hello", new Context()));

        assertEquals(1, counter.get());
    }

}
