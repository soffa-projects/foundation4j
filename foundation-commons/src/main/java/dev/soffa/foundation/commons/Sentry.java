package dev.soffa.foundation.commons;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class Sentry {

    private static SentryProvider instance = new SentryProvider.DefaultAdapter();

    private Sentry() {}

    public static void setInstance(SentryProvider instance) {
        Sentry.instance = instance;
    }

    public static SentryProvider getInstance() {
        return instance;
    }


}
