package dev.soffa.foundation.extra.notifications;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.http.DefaultHttpClient;
import dev.soffa.foundation.commons.http.HttpClient;
import dev.soffa.foundation.commons.http.HttpResponse;
import dev.soffa.foundation.error.TechnicalException;
import lombok.NoArgsConstructor;
import org.checkerframework.com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class SlackNotificationAgent implements NotificationAgent {

    private String webhook;
    private final HttpClient client = DefaultHttpClient.newInstance();

    public SlackNotificationAgent(String webhook) {
        this.webhook = webhook;
    }

    @Override
    public void notify(String message, Map<String, String> context) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        blocks.add(ImmutableMap.of(
            "type", "section",
            "text", ImmutableMap.of("type", "mrkdwn", "text", message)
        ));
        if (context != null) {
            blocks.add(ImmutableMap.of(
                "type", "context",
                "elements", context.entrySet().stream().map(e -> {
                    return ImmutableMap.of(
                        "type", "mrkdwn",
                        "text", String.format("%s: %s", e.getKey(), e.getValue())
                    );
                }).collect(Collectors.toList())
            ));
        }
        String payload = Mappers.JSON.serialize(ImmutableMap.of("blocks", blocks));
        HttpResponse response = client.post(webhook, payload);
        if (!response.isOK()) {
            throw new TechnicalException("Slack notification failed: " + response.getBody());
        } else {
            COUNTER.incrementAndGet();
        }
    }
}
