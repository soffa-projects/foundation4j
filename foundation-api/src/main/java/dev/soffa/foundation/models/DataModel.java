package dev.soffa.foundation.models;

import lombok.Getter;

import java.util.Map;

@Getter
public final class DataModel<T> {

    private final T data;
    private final Map<String, Object> metadata;

    private DataModel(T data, Map<String, Object> metadata) {
        this.data = data;
        this.metadata = metadata;
    }

    public static <T> DataModel<T> of(T data, Map<String, Object> metadata) {
        return new DataModel<>(data, metadata);
    }


    public static <T> DataModel<T> of(T data) {
        return new DataModel<>(data, null);
    }

}
