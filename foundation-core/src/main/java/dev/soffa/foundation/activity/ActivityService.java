package dev.soffa.foundation.activity;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ActivityService {

    void record(Context context, Activity activity);

    default void record(Context context, @NonNull String event, @Nullable Object data) {
        record(context, new Activity(event, Mappers.JSON.serialize(data)));
    }

    default void record(Context context, @NonNull Class<?> event, @Nullable Object data) {
        record(context, event.getSimpleName(), data);
    }

    long count(String event);

    default long count(Class<?> event) {
        return count(event.getSimpleName());
    }

}
