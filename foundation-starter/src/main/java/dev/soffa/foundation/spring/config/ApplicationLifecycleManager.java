package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.context.ApplicationLifecycle;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.soffa.foundation.spring.config.ErrorTrackingConfig.NOOP_ERROR_TRACKING;

@Component
public class ApplicationLifecycleManager implements ApplicationListener<ContextRefreshedEvent> {

    private final DB db;
    private final PubSubMessenger pubsub;
    public ApplicationContext context;

    public ApplicationLifecycleManager(ApplicationContext context,
                                       @Autowired(required = false) DB db,
                                       @Autowired(required = false) PubSubMessenger pubsub) {
        this.context = context;
        this.db = db;
        this.pubsub = pubsub;
        String errorTracking = context.getEnvironment().getProperty("app.errors_tracking.provider", NOOP_ERROR_TRACKING);
        ErrorTrackingConfig.configure(errorTracking);
    }



    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        Map<String, ApplicationLifecycle> candidates = context.getBeansOfType(ApplicationLifecycle.class);
        if (candidates.isEmpty()) {
            return;
        }
        for (ApplicationLifecycle value : candidates.values()) {
            value.onApplicationReady();
        }
    }

    public DB getDb() {
        return db;
    }

    public PubSubMessenger getPubsub() {
        return pubsub;
    }
}
