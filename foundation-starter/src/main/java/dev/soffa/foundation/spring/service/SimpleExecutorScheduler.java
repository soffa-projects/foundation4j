package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.scheduling.Scheduler;
import dev.soffa.foundation.scheduling.ServiceWorker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "app.scheduler.provider", havingValue = "simple")
public class SimpleExecutorScheduler implements Scheduler {

    private final ApplicationContext context;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public SimpleExecutorScheduler(ApplicationContext context) {
        this.context = context;
        Logger.platform.info("SimpleExecutorScheduler initialized");
    }

    @Override
    public <I, O, T extends Operation<I, O>> void enqueue(UUID uuid, Class<T> operationClass, I input, Context ctx) {
        Logger.platform.info("Operation scheduled: %s", operationClass.getName());
        final Dispatcher dispatcher = context.getBean(Dispatcher.class);
        executorService.schedule(() -> {
            dispatcher.dispatch(operationClass, input, ctx);
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
