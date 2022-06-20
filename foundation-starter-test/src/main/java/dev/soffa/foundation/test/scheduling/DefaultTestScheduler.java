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


@AllArgsConstructor
public class DefaultTestScheduler implements Scheduler {

    private ApplicationContext context;

    @Override
    public <I, O, T extends Operation<I, O>> void enqueue(UUID uuid, Class<T> operationClass, I input, Context ctx) {
        Dispatcher dispatcher = context.getBean(Dispatcher.class);
        dispatcher.dispatch(operationClass, input, ctx);
    }

    @Override
    public void scheduleRecurrently(String cronId, String cron, ServiceWorker worker) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                worker.tick();
            }
        };
        timer.schedule(task, 200, 1000);
    }
}

