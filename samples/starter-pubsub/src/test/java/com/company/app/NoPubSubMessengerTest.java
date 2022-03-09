package com.company.app;

import dev.soffa.foundation.messages.pubsub.PubSubMessenger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = {"app.pubsub.enabled=false"})
public class NoPubSubMessengerTest {

    @Autowired(required = false)
    private PubSubMessenger messenger;

    @SneakyThrows
    @Test
    public void testConfiguration() {
        Assertions.assertNull(messenger);
    }

}
