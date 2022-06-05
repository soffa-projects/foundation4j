package dev.soffa.foundation.pubsub.app;

import dev.soffa.foundation.context.ApplicationLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ApplicationListener implements ApplicationLifecycle {

    public static AtomicBoolean onApplicationReadyCalled = new AtomicBoolean(false);

    @Override
    public void onApplicationReady() {
        onApplicationReadyCalled.set(true);
    }
}
