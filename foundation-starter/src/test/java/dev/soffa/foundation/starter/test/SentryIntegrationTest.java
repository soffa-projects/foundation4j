package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.spring.config.ErrorTrackingFactory;
import io.sentry.Sentry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

public class SentryIntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "SENTRY_DSN", matches = ".+")
    public void testSentryIntegration() {
        String dsn = System.getenv("SENTRY_DSN");
        ErrorTrackingFactory.configure(new AppConfig("test", "v1.0"), "sentry|" + dsn);
        try {
            throw new TechnicalException("This is a test.");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
