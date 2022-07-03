package dev.soffa.foundation.pubsub;


import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.action.PublishEvent;
import dev.soffa.foundation.model.Event;
import dev.soffa.foundation.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "app.pubsub.enabled=true",
    "app.pubsub.clients.default.options.mode=test",
    "app.pubsub.clients.default.subscribe=foo",
    "PUBSUB_ADDRESSES=amqp://embedded"
})
public class ActionDispatcherTest extends BaseTest {

    @Autowired
    private Dispatcher dispatcher;

    @Test
    public void testPublishEvent() {
        Context ctx = Context.create();
        dispatcher.dispatch(PublishEvent.class, new Event("op1", "data1"), ctx);
        assertTrue(true);
    }
}
