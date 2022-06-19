package dev.soffa.foundation.extra.notifications;

import dev.soffa.foundation.commons.http.DefaultHttpClient;
import dev.soffa.foundation.commons.http.HttpClient;
import dev.soffa.foundation.commons.http.HttpResponse;
import dev.soffa.foundation.error.TechnicalException;
import org.checkerframework.com.google.common.collect.ImmutableMap;

public class SlackNotificationClient {

    public static void send(String webhook, String message) {
        HttpClient client = DefaultHttpClient.newInstance();
        HttpResponse response = client.post(webhook, ImmutableMap.of(
            "text", message
        ));
        if (!response.isOK()) {
            throw new TechnicalException("Slack notification failed: " + response.getBody());
        }
    }

}
