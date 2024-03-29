package dev.soffa.foundation.spring.service;

import com.google.common.collect.EvictingQueue;
import dev.soffa.foundation.activity.Activity;
import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.Context;

public class DefaultActivityService implements ActivityService {

    private final EvictingQueue<Activity> queue = EvictingQueue.create(10_000);

    @Override
    public void record(Activity activity, Context context) {
        queue.add(activity);
        Logger.platform.debug("New activity record: %s -- %s", activity.getEvent(), activity.getData());
    }

    @Override
    public long count(String event) {
        long value = queue.stream().filter(activity -> event.equalsIgnoreCase(activity.getEvent())).count();
        Logger.platform.debug("activities.%s.count = %s", event, value);
        return value;
    }
}
