package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.activity.Activity;
import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.OperationSideEffects;
import dev.soffa.foundation.core.Hooks;
import dev.soffa.foundation.core.SideEffectsHandler;
import dev.soffa.foundation.core.model.Serialized;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.model.Event;
import dev.soffa.foundation.model.HookEntry;
import dev.soffa.foundation.scheduling.DelayedOperation;
import dev.soffa.foundation.scheduling.OperationScheduler;
import dev.soffa.foundation.spring.SpringContextUtil;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
//@ConditionalOnMissingBean(SideEffectsHandler.class)
public class DefaultSideEffectsHandler implements SideEffectsHandler {

    private final ApplicationContext context;
    private ActivityService activities;
    private OperationScheduler scheduler;
    private Hooks hooks;
    private PubSubMessenger pubSub;
    private TimeSeriesProvider tsp;

    private boolean initialized;

    public DefaultSideEffectsHandler(ApplicationContext context) {
        this.context = context;
    }

    private void bootstrap() {
        if (!initialized) {
            initialized = true;
            this.activities = SpringContextUtil.findBean(context, ActivityService.class).orElse(null);
            this.pubSub = SpringContextUtil.findBean(context, PubSubMessenger.class).orElse(null);
            if (pubSub == null) {
                Logger.app.warn("[sideffect] No PubSubClient registered, events will not be sent");
            }
            this.hooks = SpringContextUtil.findBean(context, Hooks.class).orElse(null);
            if (this.hooks == null) {
                Logger.app.warn("[sideffect] No HookProvider registered, hooks will be discarded");
            }
            this.tsp = SpringContextUtil.findBean(context, TimeSeriesProvider.class).orElse(null);
            if (this.tsp == null) {
                Logger.app.warn("[sideffect] No TimeSeriesProvider registered, dataPoints will be discarded");
            }
            this.scheduler = SpringContextUtil.findBean(context, OperationScheduler.class).orElse(null);

        }
    }

    @Override
    public void enqueue(String operationName, String uuid, OperationSideEffects sideEffects, Context context) {
        bootstrap();
        for (HookEntry hook : sideEffects.getHooks()) {
            hooks.enqueue(hook, context);
        }
        for (Activity activity : sideEffects.getActivities()) {
            activities.record(activity, context);
        }
        for (DelayedOperation<?> op : sideEffects.getDelayedJobs()) {
            scheduler.enqueue(op.getUuid(), op.getOperation(), Serialized.of(op.getInput()), context);
        }
        if (pubSub != null) {
            for (Event event : sideEffects.getEvents()) {
                pubSub.publish(event.getTarget(), MessageFactory.create(event.getOperation(), event.getPayload()));
            }
        }
        if (tsp!=null) {
            tsp.getWriter().write(sideEffects.getDataPoints());
        }
    }

}
