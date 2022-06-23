package dev.soffa.foundation.application.tracking;

import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.model.Authentication;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import io.sentry.protocol.SentryId;
import io.sentry.protocol.User;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@AllArgsConstructor
public class SentrySentryProvider implements SentryProvider {

    private AppConfig appConfig;

    @Override
    public void captureException(Throwable e) {
        Sentry.captureException(e);
    }


    @Override
    public void captureEvent(Context context, @Nullable String messageId, String message, EventLevel level) {
        SentryEvent e = new SentryEvent(DateUtil.now());
        if (messageId!=null && TextUtil.isNotEmpty(messageId)) {
            e.setEventId(new SentryId(DigestUtil.makeUUID(messageId)));
        }
        if (context != null) {
            Authentication auth = context.getAuthentication();
            if (auth != null) {
                User user = new User();
                user.setId(auth.getUserId());
                user.setUsername(auth.getUsername());
                user.setIpAddress(context.getIpAddress());
                e.setUser(user);
            }
            if (context.hasApplicationId()) {
                e.setExtra("application", context.getApplicationId());
            }
            if (context.hasTenant()) {
                e.setExtra("tenant", context.getTenantId());
            }
        }

        e.setRelease(appConfig.getVersion());
        e.setServerName(appConfig.getName());

        Message msg = new Message();
        msg.setMessage(message);
        e.setLevel(SentryLevel.valueOf(level.name()));
        e.setMessage(msg);
        Sentry.captureEvent(e);
    }

}
