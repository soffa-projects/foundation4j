package dev.soffa.foundation.data;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.RandomUtil;
import dev.soffa.foundation.data.app.MessageDao;
import dev.soffa.foundation.data.app.TenantMessageDao;
import dev.soffa.foundation.data.app.model.Message;
import dev.soffa.foundation.extra.jobs.PendingJobRepo;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.PagingConstants;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.multitenancy.TenantsLoader;
import dev.soffa.foundation.test.BaseTest;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {"app.test.scheduler=mocked", "app.name=test007"})
public class EntityRepositoryTest extends BaseTest {

    @Inject
    private MessageDao messages;

    @Inject
    private TenantMessageDao tenantMessages;

    @Inject
    private TenantsLoader tenantsLoader;

    @Inject
    private DB db;

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

        assertEquals(PagingConstants.DEFAULT.getSize(), messages.findAll().getData().size());
        assertEquals(10, messages.findAll(new Paging(1, 10)).getData().size());

        assertEquals(30, messages.findAll(new Paging(6, 194)).getData().size());

        assertEquals(TenantId.DEFAULT, messages.resolveTenant());
        assertNotNull(messages.get(ImmutableMap.of("id", "msg_1")));


        assertEquals(10, messages.findAll(new Paging(0, 10)).getPaging().getCount());
        assertEquals(10, messages.findAll(new Paging(-1, 10)).getPaging().getCount());

        for (String tenant : tenantsLoader.getTenantList()) {
            Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> db.isTenantReady(tenant));
        }

        for (String tenant : tenantsLoader.getTenantList()) {
            TenantId tenantId = TenantId.of(tenant);
            // MessagesDAO is locked to TenantID.DEFAULT, so no matter the tenant, it should return the same result.
            assertEquals(generatedMessagesCount, messages.count(tenantId), () -> {
                return "Messages count for tenant " + tenantId + " is not equal to " + generatedMessagesCount;
            });

            // TenantMessageDAO is not locked
            assertEquals(0, tenantMessages.count(tenantId));

            TenantHolder.use(tenantId, () -> {
                assertEquals(generatedMessagesCount, messages.count());
                assertEquals(0, tenantMessages.count());
            });

            int generated = 1;
            for (int i = 0; i < generated; i++) {
                assertEquals(tenantId, tenantMessages.resolveTenant(tenantId));
                tenantMessages.insert(tenantId, new Message("msg_" + i, RandomUtil.nextString(32)));
            }

            assertEquals(generated, tenantMessages.count(tenantId));

            // MessagesDAO should not change
            assertEquals(generatedMessagesCount, messages.count(tenantId));


        }
    }
}
