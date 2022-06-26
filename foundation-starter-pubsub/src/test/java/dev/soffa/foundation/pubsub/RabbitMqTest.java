package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.error.ControlledRandomFailureError;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.pubsub.app.ApplicationListener;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(properties = {
    "app.name=sample",
    "app.pubsub.enabled=true",
    "app.pubsub.clients.default.broadcasting=foundation",
    "app.pubsub.clients.default.options.mode=test",
    "app.pubsub.clients.default.subscribe=foo,global*",
    "PUBSUB_ADDRESSES=amqp://embedded"
    //"PUBSUB_ADDRESSES=amqp://guest:guest@localhost:5672",
})
@ActiveProfiles({"test", "foundation-pubsub"})
@AutoConfigureMockMvc
public class RabbitMqTest {

    @Autowired
    private PubSubMessenger messenger;

    @Test
    public void testListener() {
        Assertions.assertTrue(ApplicationListener.onApplicationReadyCalled.get());
    }

    @SneakyThrows
    @Test
    public void testClient() {
        Assertions.assertNotNull(messenger);
        AtomicLong counter = new AtomicLong(0);
        AtomicLong failures = new AtomicLong(0);
        final double errorRate = 10 / 100.0;
        messenger.subscribe(message -> {
            if (Math.random() < errorRate) {
                // Induce random failure
                failures.incrementAndGet();
                throw new ControlledRandomFailureError("TRIGGER_MQ_RETRY");
            }
            counter.incrementAndGet();
            return Optional.of("Hello");
        });

        messenger.publish(MessageFactory.create("operation-test"));
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> counter.get() > 0);
        Assertions.assertTrue(failures.get() >= 0);
    }

    @SneakyThrows
    @Test
    public void testReply() {
        Assertions.assertNotNull(messenger);
        messenger.subscribe(message -> Optional.of("Hello"));
        String response = messenger.request("sample", MessageFactory.create("operation-test"), String.class).get();
        assertEquals("Hello", response);
    }

    @Test
    public void testBroadcast() {
        AtomicBoolean consumed = new AtomicBoolean(false);
        messenger.subscribe(message -> {
            consumed.set(true);
            return Optional.empty();
        });
        messenger.publish("*", MessageFactory.create("broadcast-operation-test"));
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(consumed::get);
    }

    @Test
    @Disabled
    public void testDLQ() {
        Assertions.assertNotNull(messenger);

        AtomicLong failures = new AtomicLong(0);
        messenger.subscribe(message -> {
            failures.incrementAndGet();
            throw new TechnicalException("RANDOM_FAILURE");
        });

        int messagesCount = 20;
        for (int i = 0; i < messagesCount; i++) {
            messenger.publish(MessageFactory.create("operation-test-" + i));
        }
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            // In test mode messages are attempted twice
            return messagesCount * 2 == failures.get();
        });
    }

}
