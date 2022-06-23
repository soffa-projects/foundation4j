package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.application.tracking.SentrySentryProvider;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Sentry;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.error.TechnicalException;

public final class ErrorTrackingFactory {

    public static final String NOOP_ERROR_TRACKING = "none";
    public static final String SENTRY_PROVIDER = "sentry";
    private static final Logger LOG = Logger.get(ApplicationLifecycleManager.class);

    private ErrorTrackingFactory() {}

    public static void configure(AppConfig appConfig, String provider) {
        if (NOOP_ERROR_TRACKING.equalsIgnoreCase(provider) || TextUtil.isEmpty(provider)) {
            LOG.info("No error tracking provider found.");
            return;
        }
        String[] config = provider.split("\\|");
        if (SENTRY_PROVIDER.equals(config[0]) || config[0].contains("sentry.io")) {
            String dsn = config.length == 2 ? config[1] : config[0];
            io.sentry.Sentry.init(options -> {
                options.setDsn(dsn);
                // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
                // We recommend adjusting this value in production.
                options.setTracesSampleRate(1.0);
                // When first trying Sentry it's good to see what the SDK is doing:
                options.setDebug(LOG.isDebugEnabled());
            });
            Sentry.setInstance(new SentrySentryProvider(appConfig));
        } else {
            throw new TechnicalException("Unsupported error tracking provider: %s", provider);
        }
    }
}
