package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.model.Serialized;
import dev.soffa.foundation.scheduling.OperationScheduler;
import dev.soffa.foundation.scheduling.ServiceWorker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(value = "app.scheduler.provider", havingValue = "simple")
public class SimpleExecutorOperationScheduler implements OperationScheduler {

    private final ApplicationContext context;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);
    private Dispatcher dispatcher;

    public static final AtomicLong COUNTER = new AtomicLong(0);

    public SimpleExecutorOperationScheduler(ApplicationContext context) {
        this.context = context;
        Logger.platform.info("SimpleExecutorScheduler initialized");
    }

    @Override
    public void enqueue(UUID uuid, String operationName, Serialized input, Context ctx) {
        COUNTER.incrementAndGet();
        Logger.platform.info("Operation scheduled: %s", operationName);
        if (dispatcher == null) {
            dispatcher = context.getBeansOfType(Dispatcher.class).values().iterator().next();
        }
        final String serializedContext = Mappers.JSON.serialize(ctx);
        executorService.schedule(() -> {
            Logger.platform.info("Processing scheduled operation: %s", operationName);
            COUNTER.decrementAndGet();
            try {
                dispatcher.dispatch(operationName, input, serializedContext);
            }catch (Exception e) {
                Logger.platform.error(e);
            }
        }, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleRecurrently(String cronId, String cron, ServiceWorker worker) {
        executorService.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                worker.tick();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}
