package dev.soffa.foundation.extra.notifications;

import dev.soffa.foundation.commons.http.DefaultHttpClient;
import dev.soffa.foundation.commons.http.HttpClient;
import dev.soffa.foundation.commons.http.HttpResponse;
import dev.soffa.foundation.error.TechnicalException;
import lombok.NoArgsConstructor;
import org.checkerframework.com.google.common.collect.ImmutableMap;

@NoArgsConstructor
public class SlackNotificationAgent implements NotificationAgent {

    private String webhook;
    private final HttpClient client = DefaultHttpClient.newInstance();

    public SlackNotificationAgent(String webhook) {
        this.webhook = webhook;
    }

    @Override
    public void notify(String message) {
        HttpResponse response = client.post(webhook, ImmutableMap.of(
            "text", message
        ));
        if (!response.isOK()) {
            throw new TechnicalException("Slack notification failed: " + response.getBody());
        }else {
            COUNTER.incrementAndGet();
        }
    }
}
