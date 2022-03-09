package com.company.app;

import com.company.app.gateways.*;
import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.IdGenerator;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.DataStore;
import dev.soffa.foundation.data.EntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DataStoreTest {

    @Autowired
    private DB db;

    @Test
    public void testSerialization() {
        MessageEntity e = new MessageEntity(
            new MessageId(IdGenerator.shortUUID("msg_")),
            IdGenerator.shortUUID("req_"),
            MessageStatus.PENDING,
            null,
            null,
            0,
            ImmutableMap.of("key", "value"),
            new PaymentOptions(new MobileMoneyOption(
                "00000000", "PT", "vphone", null, null
            )),
            new Date()
        );
        String output = Mappers.JSON_FULLACCESS.serialize(e);
        MessageEntity e2 = Mappers.JSON_FULLACCESS.deserialize(output, MessageEntity.class);

        assertNotNull(e2.getPaymentOptions());
        assertNotNull(e2.getPaymentOptions().getMobileMoney());
    }

    @Test
    public void testEntityRepository() {
        DataStore repo = db.newStore();
        long initialCount = repo.count(MessageEntity.class);
        assertTrue(initialCount >= 0);
        MessageEntity e = new MessageEntity(
            new MessageId(IdGenerator.shortUUID("msg_")),
            IdGenerator.shortUUID("req_"),
            MessageStatus.PENDING,
            null,
            null,
            0,
            ImmutableMap.of("key", "value"),
            new PaymentOptions(new MobileMoneyOption(
                "00000000", "PT", "vphone", null, null
            )),
            new Date()
        );
        MessageEntity saved = repo.insert(e);
        assertNotNull(saved);
        assertEquals(saved.getId(), e.getId());
        assertEquals(initialCount + 1, repo.count(MessageEntity.class));

        EntityRepository<MessageEntity> messages = db.newEntityRepository(MessageEntity.class);
        assertEquals(initialCount + 1, messages.count());

        e.setStatus(MessageStatus.DELIVERED);
        messages.update(e);


        MessageEntity loaded = messages.get("id = :id", ImmutableMap.of("id", saved.getId())).orElse(null);
        assertNotNull(loaded);
        assertEquals(saved.getRequestId(), loaded.getRequestId());
        assertEquals(MessageStatus.DELIVERED, loaded.getStatus());
        assertNotNull(loaded.getPaymentOptions());
        assertNotNull(loaded.getPaymentOptions().getMobileMoney());
        assertEquals("PT", loaded.getPaymentOptions().getMobileMoney().getCountry());

        loaded = messages.findById(saved.getId()).orElse(null);
        assertNotNull(loaded);
        assertEquals(saved.getRequestId(), loaded.getRequestId());

        assertEquals(1, messages.delete(saved));
        assertEquals(initialCount, messages.count());

        List<MessageEntity> reloaded = messages.find("id = :id", ImmutableMap.of("id", saved.getId()));
        assertEquals(initialCount, reloaded.size());

        List<MessageEntity> allMessages = messages.findAll();
        assertEquals(initialCount, allMessages.size());
    }

}
