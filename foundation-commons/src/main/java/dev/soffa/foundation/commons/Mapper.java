package dev.soffa.foundation.commons;


import lombok.SneakyThrows;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Mapper {

    <T> T convert(Object data, Class<T> type);

    String fromXml(String xmlInput);

    @SneakyThrows
    <T> T fromXml(String xmlInput, String root, Class<T> kind);

    String serialize(Object data);

    <T> T deserialize(String data, Class<T> type);

    <T> T deserialize(byte[] data, Class<T> type);

    String prettyPrint(Object data);

    @SneakyThrows
    void serializeToFile(Object content, File file);

    @SneakyThrows
    <T> Map<String, T> deserializeMap(String input);

    @SneakyThrows
    <T> Map<String, T> deserializeMap(InputStream input);

    @SneakyThrows
    <T> Map<String, T> deserializeMap(InputStream input, Class<T> type);

    @SneakyThrows
    <T> Map<String, T> deserializeMap(String input, Class<T> type);

    @SneakyThrows
    <T> T deserializeParametricType(String input, Class<?> rawType, Class<?>... parameterClasses);

    @SneakyThrows
    <T> List<T> deserializeList(String input, Class<T> type);

    @SneakyThrows
    <T> List<T> deserializeList(InputStream input, Class<T> type);

    @SuppressWarnings("unchecked")
    <E> Map<String, E> toMap(Object input, Class<E> valueClass);

    default Map<String, Object> toMap(Object input) {
        return toMap(input, Object.class);
    }
}
