package dev.soffa.foundation.extra.notifications;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.http.DefaultHttpClient;
import dev.soffa.foundation.commons.http.HttpClient;
import dev.soffa.foundation.commons.http.HttpResponse;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.extra.notifications.slack.Block;
import lombok.NoArgsConstructor;
import org.checkerframework.com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class SlackNotificationAgent implements NotificationAgent {

    private static final String TYPE = "type";

    private String webhook;
    private final HttpClient client = DefaultHttpClient.newInstance();

    public SlackNotificationAgent(String webhook) {
        this.webhook = webhook;
    }

    @Override
    public void notify(String message, Map<String, String> context) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(Block.section(Block.markdown(message)));
        if (context != null) {
            blocks.add(
                new Block(
                    "context",
                    context.entrySet().stream().map(e -> Block.markdown(
                        String.format("%s: %s", e.getKey(), e.getValue()))
                    ).collect(Collectors.toList())
                )
            );
        }
        String payload = Mappers.JSON_DEFAULT.serialize(ImmutableMap.of("blocks", blocks));
        HttpResponse response = client.post(webhook, payload);
        if (!response.isOK()) {
            Logger.platform.info("Slack notification failed: %s", response.getBody());
            throw new TechnicalException("Slack notification failed: " + response.getBody());
        } else {
            COUNTER.incrementAndGet();
        }
    }
}
