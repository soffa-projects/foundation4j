package dev.soffa.foundation.spring.config.scheduling;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.scheduling.Scheduler;
import dev.soffa.foundation.spring.service.OperationDispatcher;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SchedulerImpl implements Scheduler {

    private final JobScheduler jobScheduler;
    private final ApplicationContext context;
    private Dispatcher dispatcher;

    public SchedulerImpl(JobScheduler jobScheduler, ApplicationContext context) {
        this.jobScheduler = jobScheduler;
        this.context = context;
    }

    @Override
    public <I, O, T extends Operation<I, O>> void enqueue(Class<T> operationClass, I input, Context ctx) {
        if (dispatcher == null) {
            dispatcher = context.getBeansOfType(OperationDispatcher.class).values().iterator().next();
        }
        TenantHolder.useDefault(() -> {
            jobScheduler.enqueue(() -> dispatcher.dispatch(operationClass, input, ctx));
            return Optional.empty();
        });
    }

}
