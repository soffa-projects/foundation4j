package dev.soffa.foundation.spring.config.notifications;

import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.extra.notifications.NoopNotificationAgent;
import dev.soffa.foundation.extra.notifications.NotificationAgent;
import dev.soffa.foundation.extra.notifications.SlackNotificationAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationAgentConfiguration {

    private static final String NOOP_AGENT = "noop";

    @Bean
    public NotificationAgent createNotificationAgent(@Value("${slack.webhook:${SLACK_WEBHOOK:noop}}") String webhook) {
        if (TextUtil.isEmpty(webhook) || NOOP_AGENT.equalsIgnoreCase(webhook)) {
            return new NoopNotificationAgent();
        }
        return new SlackNotificationAgent(webhook);
    }

}
