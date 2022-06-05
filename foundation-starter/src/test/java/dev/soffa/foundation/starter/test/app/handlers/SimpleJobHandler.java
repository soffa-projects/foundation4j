package dev.soffa.foundation.starter.test.app.handlers;

import dev.soffa.foundation.annotation.Cron;
import dev.soffa.foundation.scheduling.ServiceWorker;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;


@Component
public class SimpleJobHandler implements ServiceWorker {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Cron(Cron.EVERY_5_SECONDS)
    public void tick() {
        COUNTER.incrementAndGet();
    }

    public static int getCount() {
        return COUNTER.get();
    }
}
