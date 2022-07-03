package dev.soffa.foundation.data.spring.scheduling;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.model.Serialized;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.scheduling.OperationScheduler;
import dev.soffa.foundation.scheduling.ServiceWorker;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(value = "app.scheduler.provider", havingValue = "jobrunr")
class JobRunrOperationScheduler implements OperationScheduler {

    private final JobScheduler jobScheduler;
    private final ApplicationContext context;
    private Dispatcher dispatcher;

    public JobRunrOperationScheduler(JobScheduler jobScheduler, ApplicationContext context) {
        this.jobScheduler = jobScheduler;
        this.context = context;
        Logger.platform.info("JobRunrScheduler initialized");
    }

    @Override
    public void scheduleRecurrently(String cronId, String cron, ServiceWorker worker) {
        jobScheduler.scheduleRecurrently(cronId, cron, worker::tick);
    }

    @Override
    public void enqueue(UUID uuid, String operationName, final Serialized input, final Context ctx) {
        if (dispatcher == null) {
            dispatcher = context.getBeansOfType(Dispatcher.class).values().iterator().next();
        }
        final String serialzedContext = Mappers.JSON_DEFAULT.serialize(ctx);
        JobId jobId = TenantHolder.useDefault(() -> {
            // EL
            return jobScheduler.enqueue(uuid, () -> dispatcher.dispatch(operationName, input, serialzedContext));
        });
        Logger.platform.info("Operation scheduled: %s --> %s | context=%s", operationName, jobId, serialzedContext);
    }



}
