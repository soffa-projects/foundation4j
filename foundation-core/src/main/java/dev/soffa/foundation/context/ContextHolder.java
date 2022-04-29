package dev.soffa.foundation.context;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.http.HttpContextHolder;
import dev.soffa.foundation.error.FunctionalException;
import dev.soffa.foundation.model.UserInfo;
import io.opentelemetry.api.trace.Span;

import java.util.Optional;

public final class ContextHolder {

    private static final ThreadLocal<Context> CURRENT = new InheritableThreadLocal<>();

    private ContextHolder() {
    }

    public static void set(Context value) {

        if (value == null) {
            CURRENT.remove();
            HttpContextHolder.clear();
            Logger.setContext(null);
        } else {
            setAttribute("tenant", value.getTenantId());
            setAttribute("sender", value.getSender());
            if (value.getAuthentication() != null) {
                setAttribute("user", value.getAuthentication().getUsername());
                setAttribute("application", value.getAuthentication().getApplication());
                setAttribute("user_tenant", value.getAuthentication().getTenantId());
                setAttribute("live_mode", value.getAuthentication().isLiveMode());
                UserInfo profile = value.getAuthentication().getProfile();
                if (profile != null) {
                    setAttribute("user_country", profile.getCountry());
                    setAttribute("user_city", profile.getCity());
                }
            }
            CURRENT.set(value);
            Logger.setContext(value.getContextMap());
            HttpContextHolder.set(value.getHeaders());
        }
    }

    public static void setAttribute(String key, String value) {
        Span span = Span.current();
        if (TextUtil.isNotEmpty(value) && span != null) {
            span.setAttribute(key, value);
        }
    }

    public static void setAttribute(String key, long value) {
        Span span = Span.current();
        span.setAttribute(key, value);
    }

    public static void setAttribute(String key, boolean value) {
        Span span = Span.current();
        span.setAttribute(key, value);
    }

    public static void clear() {
        set(null);
    }

    public static boolean isEmpty() {
        return CURRENT.get() == null;
    }

    public static Optional<Context> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Context inheritOrCreate() {
        return Optional.ofNullable(CURRENT.get()).orElse(new Context());
    }

    public static Context require() {
        return Optional.ofNullable(CURRENT.get()).orElseThrow(() -> new FunctionalException("MISSING_CONTEXT"));
    }

}
