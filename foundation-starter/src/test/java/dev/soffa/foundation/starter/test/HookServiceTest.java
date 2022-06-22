package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.extra.notifications.NoopNotificationAgent;
import dev.soffa.foundation.hooks.HookService;
import dev.soffa.foundation.hooks.action.ProcessHookInput;
import dev.soffa.foundation.hooks.model.Hook;
import org.checkerframework.com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
        Hook hook1 = hookService.getHook(CREATED_ACCOUNT);
        assertNotNull(hook1);
        Hook hook2 = hookService.getHook(CREATED_ACCOUNT);
        assertNotNull(hook2);
        assertEquals(hook1, hook2);
    }

    @Test
    public void testHookRun() {
        long counter = NoopNotificationAgent.COUNTER.get();
        ProcessHookInput input = new ProcessHookInput(CREATED_ACCOUNT, null, ImmutableMap.of(
            "email", "foo@local.dev",
            "id", "acc_001",
            "name", "Bantu",
            "apiKey", "pi_091029019201920912"
        ));
        assertEquals(2, hookService.process(Context.create(), input));
        assertEquals(counter + 1, NoopNotificationAgent.COUNTER.get());
    }
}
