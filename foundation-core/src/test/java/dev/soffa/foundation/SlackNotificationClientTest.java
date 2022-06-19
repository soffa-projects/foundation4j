package dev.soffa.foundation;

import dev.soffa.foundation.extra.notifications.SlackNotificationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

public class SlackNotificationClientTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "SLACK_WEBHOOK", matches = ".*")
    public void test() {
        SlackNotificationClient.send(
            System.getenv("SLACK_WEBHOOK"),
            "Hello world"
        );
    }

}
