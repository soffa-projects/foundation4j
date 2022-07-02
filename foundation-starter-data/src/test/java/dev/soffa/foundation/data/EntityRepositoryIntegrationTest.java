package dev.soffa.foundation.data;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.RandomUtil;
import dev.soffa.foundation.data.app.MessageDao;
import dev.soffa.foundation.data.app.TenantMessageDao;
import dev.soffa.foundation.data.app.model.Message;
import dev.soffa.foundation.extra.jobs.PendingJobRepo;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.multitenancy.TenantsLoader;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ContextConfiguration(initializers = {EntityRepositoryIntegrationTest.Initializer.class})
@SpringBootTest(properties = {"app.test.scheduler=mocked", "app.name=docker-test"})
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
@Testcontainers
public class EntityRepositoryIntegrationTest {


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

    @SuppressWarnings("rawtypes")
    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11")
        .withDatabaseName("foundation")
        .withUsername("user1")
        .withPassword("passw1");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            assertTrue(container.isRunning());
            String url = String.format("postgres://%s:%s@%s:%d/%s",
                container.getUsername(),
                container.getPassword(),
                container.getHost(),
                container.getMappedPort(5432),
                container.getDatabaseName()
            );
            String tenantsUrl = url + "?schema=__tenant__";
            TestPropertyValues.of(
                "app.test.scheduler=mocked",
                "app.db.datasources.__tenant__.url=" + tenantsUrl,
                "app.db.datasources.default.url=" + url
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }


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

        for (String tenant : tenantsLoader.getTenantList()) {
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> db.isTenantReady(tenant));
        }

        for (String tenant : tenantsLoader.getTenantList()) {
            TenantId tenantId = TenantId.of(tenant);
            // MessagesDAO is locked to TenantID.DEFAULT, so no matter the tenant, it should return the same result.
            assertEquals(generatedMessagesCount, messages.count(tenantId));

            // TenantMessageDAO is not locked
            assertEquals(0, tenantMessages.count(tenantId));

            TenantHolder.use(tenantId, () -> {
                assertEquals(generatedMessagesCount, messages.count());
                assertEquals(0, tenantMessages.count());
            });

            int generated = 1;
            for (int i = 0; i < generated; i++) {
                messages.insert(tenantId, new Message("msg_" + i, RandomUtil.nextString(20)));
            }

            assertEquals(generated, tenantMessages.count(tenantId));

            // MessagesDAO should not change
            assertEquals(generatedMessagesCount, messages.count(tenantId));


        }
    }


}
