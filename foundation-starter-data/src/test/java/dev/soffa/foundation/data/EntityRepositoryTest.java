package dev.soffa.foundation.data;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.RandomUtil;
import dev.soffa.foundation.data.app.MessageDao;
import dev.soffa.foundation.data.app.model.Message;
import dev.soffa.foundation.extra.jobs.PendingJobRepo;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.test.BaseTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EntityRepositoryTest extends BaseTest {

    @Inject
    private MessageDao messages;

    @Inject
    private PendingJobRepo pendingJobs;

    @Test
    public void testPendingJobs() {
        assertEquals(0, pendingJobs.count());
    }

    @Test
    public void testPaging() {
        assertNotNull(messages);
        assertEquals(0, messages.count());
        final int generatedMessagesCount = 1000;
        for (int i = 0; i < generatedMessagesCount; i++) {
            messages.insert(new Message("msg_" + i, RandomUtil.nextString(20)));
        }
        assertEquals(generatedMessagesCount, messages.count());

        assertEquals(Paging.DEFAULT.getSize(), messages.findAll().getData().size());
        assertEquals(10, messages.findAll(new Paging(1, 10)).getData().size());
        assertEquals(Paging.DEFAULT_MAX_SIZE, messages.findAll(new Paging(1, 10_000).cap()).getData().size());

        assertEquals(30, messages.findAll(new Paging(6, 194)).getData().size());

        assertNotNull(messages.get(ImmutableMap.of("id", "msg_1")));


        assertEquals(10, messages.findAll(new Paging(0, 10)).getPaging().getCount());
        assertEquals(10, messages.findAll(new Paging(-1, 10)).getPaging().getCount());
    }
}
