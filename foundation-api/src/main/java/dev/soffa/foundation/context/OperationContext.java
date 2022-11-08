package dev.soffa.foundation.context;

import dev.soffa.foundation.model.DataPoint;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;
import java.util.Map;

public interface OperationContext extends BaseContext {


    void hook(String name, String subject, Map<String, Object> data);

    void hook(String name, Map<String, Object> data);

    void event(String name, Map<String, Object> data);

    void dataPoint(@NonNull String metric, @NonNull Map<String, String> tags, @NonNull Map<String, Object> fields, @NonNull Date time);

    void dataPoint(@NonNull String metric, @NonNull Map<String, String> tags, @NonNull Map<String, Object> fields);

    void dataPoint(@NonNull DataPoint point);

    void activity(@NonNull String event, String subject, Object data);

    // <E, O, T extends Operation<E, O>> void delayed(String uuid, Class<T> operationClass, E input);

    Context getInternal();
}
