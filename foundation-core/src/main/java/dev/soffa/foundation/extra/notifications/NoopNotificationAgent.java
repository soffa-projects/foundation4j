package dev.soffa.foundation.extra.notifications;

import dev.soffa.foundation.commons.Logger;

import java.util.Map;

public class NoopNotificationAgent implements NotificationAgent {

    private static final Logger LOG = Logger.get(NoopNotificationAgent.class);

    @Override
    public void notify(String message, Map<String, String> context) {
        LOG.info("[notification] %s", message);
        COUNTER.incrementAndGet();
    }
}
