package dev.soffa.foundation.activity;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ActivityService {

    void record(Activity activity, Context context);

    default void record( @NonNull String event, @Nullable Object data, Context context) {
        record(new Activity(event, null, Mappers.JSON_DEFAULT.serialize(data)), context);
    }

    default void record(@NonNull Class<?> event, @Nullable Object data, Context context) {
        record(event.getSimpleName(), data, context);
    }

    long count(String event);

    default long count(Class<?> event) {
        return count(event.getSimpleName());
    }

}
