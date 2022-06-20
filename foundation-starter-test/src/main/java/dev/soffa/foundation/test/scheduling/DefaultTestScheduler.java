package dev.soffa.foundation.test.scheduling;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.scheduling.Scheduler;
import dev.soffa.foundation.scheduling.ServiceWorker;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@AllArgsConstructor
public class DefaultTestScheduler implements Scheduler {

    private ApplicationContext context;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    @Override
    public <I, O, T extends Operation<I, O>> void enqueue(UUID uuid, Class<T> operationClass, I input, Context ctx) {
        final Dispatcher dispatcher = context.getBean(Dispatcher.class);
        executorService.schedule(() -> {
            dispatcher.dispatch(operationClass, input, ctx);
        },1, TimeUnit.SECONDS);
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

