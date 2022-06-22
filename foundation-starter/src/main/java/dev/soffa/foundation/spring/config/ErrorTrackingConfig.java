package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.TechnicalException;
import io.sentry.Sentry;

public class ErrorTrackingConfig {

    public static final String NOOP_ERROR_TRACKING = "none";
    public static final String SENTRY_PROVIDER = "sentry";
    private static final Logger LOG = Logger.get(ApplicationLifecycleManager.class);

    public static void configure(String provider) {
        if (NOOP_ERROR_TRACKING.equalsIgnoreCase(provider) || TextUtil.isEmpty(provider)) {
            LOG.info("No error tracking provider found.");
            return;
        }
        String[] config = provider.split("\\|");
        if (SENTRY_PROVIDER.equals(config[0])) {
            Sentry.init(options -> {
                options.setDsn(config[1]);
                // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
                // We recommend adjusting this value in production.
                options.setTracesSampleRate(1.0);
                // When first trying Sentry it's good to see what the SDK is doing:
                // options.setDebug(true);
            });
        } else {
            throw new TechnicalException("Unsupported error tracking provider: %s", provider);
        }
    }
}
