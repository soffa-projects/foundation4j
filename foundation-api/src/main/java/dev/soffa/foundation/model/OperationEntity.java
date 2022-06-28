package dev.soffa.foundation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Instant;
import java.util.*;

@Getter
@NoArgsConstructor
public final class OperationEntity<T> {

    private int status;
    private T data;
    private final List<HookEntry> hooks = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private final List<DataPoint> dataPoints = new ArrayList<>();

    public OperationEntity(int status) {
        this.status = status;
    }

    public OperationEntity(T data) {
        this.data = data;
    }

    public OperationEntity(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public OperationEntity<T> addHook(String name, String subject, Map<String, Object> data) {
        hooks.add(new HookEntry(name, subject, data));
        return this;
    }

    public OperationEntity<T> addHook(String name, Map<String, Object> data) {
        return addHook(name, UUID.randomUUID().toString(), data);
    }

    public OperationEntity<T> addEvent(String name, Map<String, Object> data) {
        events.add(new Event(name, data));
        return this;
    }

    public OperationEntity<T> addDataPoint(@NonNull String metric, @NonNull Map<String, String> tags, @NonNull Map<String, Object> fields, @NonNull Date time) {
        dataPoints.add(new DataPoint(metric, time, tags, fields));
        return this;
    }

    public OperationEntity<T> addDataPoint(@NonNull String metric, @NonNull Map<String, String> tags, @NonNull Map<String, Object> fields) {
        return addDataPoint(metric, tags, fields, Date.from(Instant.now()));
    }

    public OperationEntity<T> addDataPoint(@NonNull DataPoint point) {
        dataPoints.add(point);
        return this;
    }

}
