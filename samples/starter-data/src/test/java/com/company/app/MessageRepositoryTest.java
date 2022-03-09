package com.company.app;

import com.company.app.gateways.MessageRepository;
import dev.soffa.foundation.multitenancy.TenantHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messages;

    @Test
    public void testConfig() {
        TenantHolder.set("T1");
        assertTrue(messages.count() >= 0);
        TenantHolder.set("T2");
        assertTrue(messages.count() >= 0);
        TenantHolder.clear();
        assertTrue(messages.count() >= 0);
    }


}
