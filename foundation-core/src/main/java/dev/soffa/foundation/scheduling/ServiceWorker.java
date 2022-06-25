package dev.soffa.foundation.scheduling;

import dev.soffa.foundation.annotation.Sentry;

public interface ServiceWorker {

    @Sentry(label = "ServiceWorker", errorPropagation = false)
    void tick();

}
