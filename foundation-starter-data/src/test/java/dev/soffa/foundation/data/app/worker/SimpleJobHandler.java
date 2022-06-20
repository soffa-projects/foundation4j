package dev.soffa.foundation.data.app.worker;

import dev.soffa.foundation.annotation.Cron;
import dev.soffa.foundation.scheduling.ServiceWorker;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;


@Component
@Cron(Cron.EVERY_5_SECONDS)
public class SimpleJobHandler implements ServiceWorker {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static int getCount() {
        return COUNTER.get();
    }

    @Override
    public void tick() {
        COUNTER.incrementAndGet();
    }
}
