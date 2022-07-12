package dev.soffa.foundation.data.spring.service;

import dev.soffa.foundation.activity.Activity;
import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.context.OperationSideEffects;
import dev.soffa.foundation.core.Hooks;
import dev.soffa.foundation.core.action.ProcessSideEffect;
import dev.soffa.foundation.extra.jobs.PendingJob;
import dev.soffa.foundation.extra.jobs.PendingJobRepo;
import dev.soffa.foundation.extra.jobs.ProcessSideEffectInput;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.model.Event;
import dev.soffa.foundation.model.HookEntry;
import dev.soffa.foundation.scheduling.DelayedOperation;
import dev.soffa.foundation.scheduling.OperationScheduler;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Primary
public class SideEffectProcessor implements ProcessSideEffect {

    private final ApplicationContext context;
    private ActivityService activities;
    private OperationScheduler scheduler;
    private Hooks hooks;
    private PubSubMessenger pubSub;
    private TimeSeriesProvider tsp;

    private final PendingJobRepo pendingJobs;

    private boolean initialized;

    public SideEffectProcessor(ApplicationContext context, PendingJobRepo pendingJobs) {
        this.context = context;
        this.pendingJobs = pendingJobs;
    }

    private void bootstrap() {
        if (!initialized) {
            initialized = true;
            this.activities = findBean(ActivityService.class).orElse(null);
            this.pubSub = findBean(PubSubMessenger.class).orElse(null);
            if (pubSub == null) {
                Logger.app.warn("[sideffect] No PubSubClient registered, events will not be sent");
            }
            this.hooks = findBean(Hooks.class).orElse(null);
            if (this.hooks == null) {
                Logger.app.warn("[sideffect] No HookProvider registered, hooks will be discarded");
            }
            this.tsp = findBean(TimeSeriesProvider.class).orElse(null);
            if (this.tsp == null) {
                Logger.app.warn("[sideffect] No TimeSeriesProvider registered, dataPoints will be discarded");
            }
            this.scheduler = findBean(OperationScheduler.class).orElse(null);

        }
    }

    private <T> Optional<T> findBean(Class<T> clazz) {
        Map<String, T> beans = context.getBeansOfType(clazz);
        if (beans.isEmpty()) {
            return Optional.empty();
        }
        boolean hasMoreThanOne = beans.size() > 1;
        if (hasMoreThanOne) {
            throw new IllegalStateException("More than one bean of type " + clazz + " found");
        }
        return Optional.of(beans.values().iterator().next());
    }

    @Override
    public Void handle(ProcessSideEffectInput input, @NonNull OperationContext ctx) {
        Logger.app.info("Processing side effect: %s", input.getId());
        bootstrap();
        pendingJobs.consume(input.getId(), pendingJob -> {

            OperationSideEffects sideEffects = Mappers.JSON_DEFAULT.deserialize(pendingJob.getData(), OperationSideEffects.class);

            if (tsp != null && sideEffects.hasDataPoints()) {
                tsp.getWriter().write(sideEffects.getDataPoints());
                // DataPoints  processed wihtout errors, update side effects if following block files
                Logger.app.info("[%s] %d datapoints processed", input.getId(), sideEffects.getDataPoints().size());
                sideEffects.setDataPoints(null);
                updateJob(pendingJob, sideEffects);
            }

            if (scheduler != null && sideEffects.hasDelayedJobs()) {
                for (DelayedOperation<?> op : sideEffects.getDelayedJobs()) {
                    scheduler.enqueue(op.getUuid(), op.getOperation(), op.getInput(), ctx.getInternal());
                }
                // Delayed jobs processed wihtout errors, update side effects if following block files
                Logger.app.info("[%s] %d delayed jobs processed", input.getId(), sideEffects.getDelayedJobs().size());
                sideEffects.setDelayedJobs(null);
                updateJob(pendingJob, sideEffects);
            }

            if (pubSub != null && sideEffects.hasEvents()) {
                for (Event event : sideEffects.getEvents()) {
                    pubSub.publish(event.getTarget(), MessageFactory.create(event.getOperation(), event.getPayload()));
                }
                // Events processed wihtout errors, update side effects if following block files
                Logger.app.info("[%s] %d events processed", input.getId(), sideEffects.getEvents().size());
                sideEffects.setEvents(null);
                updateJob(pendingJob, sideEffects);
            }

            if (hooks != null && sideEffects.hasHooks()) {
                for (HookEntry hook : sideEffects.getHooks()) {
                    hooks.enqueue(hook, ctx.getInternal());
                }
                // Hooks processed wihtout errors, update side effects if following block files
                Logger.app.info("[%s] %d hooks processed", input.getId(), sideEffects.getHooks().size());
                sideEffects.setHooks(null);
                updateJob(pendingJob, sideEffects);
            }

            if (activities != null && sideEffects.hasActivities()) {
                for (Activity activity : sideEffects.getActivities()) {
                    activities.record(activity, ctx.getInternal());
                }
                // Activities processed wihtout errors, update side effects if following block files
                Logger.app.info("[%s] %d activities processed", input.getId(), sideEffects.getActivities().size());
                sideEffects.setActivities(null);
                updateJob(pendingJob, sideEffects);
            }

            return true;
        });
        Logger.app.info("Side effect %s processed successfully", input.getId());

        return null;
    }

    private void updateJob(PendingJob pendingJob, OperationSideEffects sideEffects) {
        pendingJob.setData(Mappers.JSON_DEFAULT.serialize(sideEffects));
        pendingJobs.update(pendingJob);

    }
}
