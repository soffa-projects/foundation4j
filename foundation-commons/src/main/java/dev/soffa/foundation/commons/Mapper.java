package dev.soffa.foundation.commons;


import dev.soffa.foundation.error.NotImplementedException;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Mapper {

    <T> T convert(Object data, Class<T> type);

    String fromXml(String xmlInput);

    <T> T fromXml(String xmlInput, String root, Class<T> kind);

    String serialize(Object data);

    byte[] serializeAsBytes(Object data);

    <T> T deserialize(String data, Class<T> type);

    @SuppressWarnings("unchecked")
    default <T> T deserialize(String data, Type type) {
        if (type instanceof Class<?>) {
            return deserialize(data, (Class<T>) type);
        }
        throw new NotImplementedException();
    }


    <T> T deserialize(byte[] data, Class<T> type);

    <T> T deserialize(InputStream source, Class<T> type);

    String prettyPrint(Object data);

    void serializeToFile(Object content, File file);

    <T> Map<String, T> deserializeMap(String input);

    <T> Map<String, T> deserializeMap(InputStream input);

    <T> Map<String, T> deserializeMap(InputStream input, Class<T> type);

    <T> Map<String, T> deserializeMap(String input, Class<T> type);

    <T> T deserializeParametricType(String input, Class<?> rawType, Class<?>... parameterClasses);

    <T> List<T> deserializeList(String input, Class<T> type);

    <T> List<T> deserializeList(InputStream input, Class<T> type);

    @SuppressWarnings("unchecked")
    <E> Map<String, E> toMap(Object input, Class<E> valueClass);

    default Map<String, Object> toMap(Object input) {
        if (input == null) {
            return new HashMap<>();
        }
        return toMap(input, Object.class);
    }

    @SuppressWarnings("unchecked")
    default <T> T deserialize(byte[] payload, Type type) {
        if (type instanceof Class<?>) {
            return deserialize(payload, (Class<T>) type);
        }
        throw new NotImplementedException();
    }

}
