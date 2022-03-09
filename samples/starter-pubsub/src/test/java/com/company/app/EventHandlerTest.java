package com.company.app;

import com.company.app.core.Echo;
import com.company.app.core.Ping;
import dev.soffa.foundation.messages.MessageFactory;
import dev.soffa.foundation.messages.MessageHandler;
import dev.soffa.foundation.metrics.MetricsRegistry;
import dev.soffa.foundation.multitenancy.TenantHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class EventHandlerTest {

    @Autowired
    private MessageHandler handler;

    @Autowired
    private MetricsRegistry metricsRegistry;

    private double getCounterValue(String op) {
        return metricsRegistry.counter("app_operation_" + op);
    }

    @Test
    public void testEventsHandler() {
        String ping = Ping.class.getName();
        String echo = Echo.class.getName();

        double pingCount = getCounterValue(ping);
        double echoCount = getCounterValue(Echo.class.getName());

        TenantHolder.use("T1", () -> {
            handler.handle(MessageFactory.create(ping)); // automatic tenant
            handler.handle(MessageFactory.create(echo, "Hello"));
            handler.handle(MessageFactory.create(ping));
        });

        assertEquals(pingCount + 2, getCounterValue(ping));
        assertEquals(echoCount + 1, getCounterValue(echo));

    }


}
