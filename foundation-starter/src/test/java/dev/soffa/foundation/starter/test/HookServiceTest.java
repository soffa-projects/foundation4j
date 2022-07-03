package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.HookService;
import dev.soffa.foundation.core.model.HookSpec;
import dev.soffa.foundation.core.model.ProcessHookInput;
import dev.soffa.foundation.extra.notifications.NoopNotificationAgent;
import org.awaitility.Awaitility;
import org.checkerframework.com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(properties = {
    "app.mail.clients.default=faker://local"
})
public class HookServiceTest {

    @Autowired
    private HookService hookService;

    public static final String CREATED_ACCOUNT = "create_account";

    @Test
    public void testHooks() {
        HookSpec hook1 = hookService.getHook(CREATED_ACCOUNT);
        assertNotNull(hook1);
        HookSpec hook2 = hookService.getHook(CREATED_ACCOUNT);
        assertNotNull(hook2);
        assertEquals(hook1, hook2);
    }

    @Test
    @Disabled
    public void testHookRun() {

        ProcessHookInput input = ProcessHookInput.create(CREATED_ACCOUNT, null, ImmutableMap.of(
            "email", "foo@local.dev",
            "id", "acc_001",
            "name", "Bantu",
            "apiKey", "pi_091029019201920912"
        ));
        long counter = NoopNotificationAgent.COUNTER.get();
        assertEquals(2, hookService.process(Context.create(), input));
        assertEquals(counter + 1, NoopNotificationAgent.COUNTER.get());

        input = ProcessHookInput.create(CREATED_ACCOUNT, null, ImmutableMap.of());
        assertEquals(2, hookService.process(Context.create(), input));

        input = ProcessHookInput.create(CREATED_ACCOUNT);
        assertEquals(2, hookService.process(Context.create(), input));

        assertEquals(0, hookService.process(Context.create(), ProcessHookInput.create("nonExistingService")));
    }

    @Test
    public void testHookEnqueue() {
        Context ctx = Context.create();
        ctx.setAccountName("the-account");
        ctx.setApplicationName("the-application");
        String subjectId = UUID.randomUUID().toString();

        final long processed = hookService.getProcessedHooks();
        hookService.enqueue(CREATED_ACCOUNT, subjectId, ImmutableMap.of(
            "email", "foo@local.dev",
            "id", "acc_001",
            "name", "Bantu",
            "apiKey", "pi_091029019201920912"
        ), ctx);

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> hookService.getProcessedHooks() >= processed + 1);

    }
}
