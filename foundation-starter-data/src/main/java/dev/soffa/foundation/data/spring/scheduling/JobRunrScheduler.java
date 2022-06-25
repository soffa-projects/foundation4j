package dev.soffa.foundation.data.spring.scheduling;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.scheduling.Scheduler;
import dev.soffa.foundation.scheduling.ServiceWorker;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
@ConditionalOnProperty(value = "app.scheduler.provider", havingValue = "jobrunr", matchIfMissing = true)
class JobRunrScheduler implements Scheduler {

    private final JobScheduler jobScheduler;
    private final ApplicationContext context;
    private Dispatcher dispatcher;


    public JobRunrScheduler(JobScheduler jobScheduler, ApplicationContext context) {
        this.jobScheduler = jobScheduler;
        this.context = context;
        Logger.platform.info("JobRunrScheduler initialized");
    }

    @Override
    public void scheduleRecurrently(String cronId, String cron, ServiceWorker worker) {
        jobScheduler.scheduleRecurrently(cronId, cron, worker::tick);
    }

    @Override
    public <I, O, T extends Operation<I, O>> void enqueue(UUID uuid, Class<T> operationClass, I input, Context ctx) {
        if (dispatcher == null) {
            dispatcher = context.getBeansOfType(Dispatcher.class).values().iterator().next();
        }
        JobId jobId = TenantHolder.useDefault(() -> {
            // EL
            return jobScheduler.enqueue(uuid, () -> dispatcher.dispatch(operationClass, input, ctx));
        });
        Logger.platform.info("Operation scheduled: %s --> %s", operationClass.getSimpleName(), jobId);
    }

}
