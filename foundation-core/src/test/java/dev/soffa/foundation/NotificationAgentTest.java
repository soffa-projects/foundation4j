package dev.soffa.foundation;

import dev.soffa.foundation.extra.notifications.NoopNotificationAgent;
import dev.soffa.foundation.extra.notifications.NotificationAgent;
import dev.soffa.foundation.extra.notifications.SlackNotificationAgent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NotificationAgentTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "SLACK_WEBHOOK", matches = ".*")
    public void test() {
        NotificationAgent agent = new SlackNotificationAgent(System.getenv("SLACK_WEBHOOK"));
        agent.notify("Hello world");
    }

    @Test
    public void testNoopAgent() {
        NotificationAgent agent = new NoopNotificationAgent();
        agent.notify("Hello world");
        assertNotNull(agent);
    }

}
