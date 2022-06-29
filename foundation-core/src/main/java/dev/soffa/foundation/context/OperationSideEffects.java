package dev.soffa.foundation.context;

import dev.soffa.foundation.activity.Activity;
import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.model.DataPoint;
import dev.soffa.foundation.scheduling.DelayedOperation;
import dev.soffa.foundation.model.Event;
import dev.soffa.foundation.model.HookEntry;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationSideEffects {

    private List<Event> events = new ArrayList<>();
    private List<Activity> activities = new ArrayList<>();
    private List<HookEntry> hooks = new ArrayList<>();
    private List<DataPoint> dataPoints = new ArrayList<>();
    private List<DelayedOperation<?>> delayedJobs = new ArrayList<>();

    public boolean isEmpty() {
        return !hasDataPoints() && !hasEvents() && !hasActivities() && !hasHooks() && !hasDelayedJobs();
    }

    public boolean hasDataPoints() {
        return CollectionUtil.isNotEmpty(dataPoints);
    }

    public boolean hasEvents() {
        return CollectionUtil.isNotEmpty(events);
    }

    public boolean hasActivities() {
        return CollectionUtil.isNotEmpty(activities);
    }

    public boolean hasHooks() {
        return CollectionUtil.isNotEmpty(hooks);
    }

    public boolean hasDelayedJobs() {
        return CollectionUtil.isNotEmpty(delayedJobs);
    }
}
