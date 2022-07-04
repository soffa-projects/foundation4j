package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.pubsub.app.ApplicationListener;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/*
@SpringBootTest(properties = {
    "app.pubsub.enabled=true",
    "PUBSUB_ADDRESSES=nats://localhost:14222"
})*/
// @ActiveProfiles({"test", "foundation-pubsub"})
// @AutoConfigureMockMvc
// @EnableNatsServer(port = 14_222)
@EnabledIfEnvironmentVariable(named = "DOCKER", matches = "true")
public class NatsIntegrationTest {

    @Autowired
    private PubSubMessenger messenger;

    @Test
    public void testListener() {
        Assertions.assertTrue(ApplicationListener.onApplicationReadyCalled.get());
    }

    @Test
    public void testClient() {
        Assertions.assertNotNull(messenger);

        AtomicLong counter = new AtomicLong(0);
        messenger.subscribe("subject-01", false, message -> {
            counter.incrementAndGet();
            return Optional.empty();
        });

        messenger.publish("subject-01", MessageFactory.create("operation-test"));

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> 1 == counter.get());
    }

}
