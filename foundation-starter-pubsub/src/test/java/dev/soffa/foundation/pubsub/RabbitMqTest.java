package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.pubsub.app.ApplicationListener;
import dev.soffa.foundation.pubsub.app.Broadcast1;
import dev.soffa.foundation.pubsub.app.Hello1;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(properties = {
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
    @RepeatedTest(3)
    public void testClient() {
        messenger.publish(MessageFactory.create(Hello1.class.getSimpleName()));
        Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> Hello1.count() > 0);
    }

    @SneakyThrows
    @Test
    public void testReply() {
        String response = messenger.request("sample", Hello1.class, String.class).get();
        assertEquals("Hello", response);
    }

    @Test
    public void testBroadcast() {
        messenger.publish("*", MessageFactory.create(Broadcast1.class.getSimpleName()));
        Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> Broadcast1.count() > 0);
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
