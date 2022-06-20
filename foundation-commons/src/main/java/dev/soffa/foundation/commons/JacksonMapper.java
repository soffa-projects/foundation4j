package dev.soffa.foundation.commons;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@AllArgsConstructor
public class JacksonMapper implements Mapper {

    private final ObjectMapper mapper;

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public <T> T convert(Object data, Class<T> type) {
        if (data == null) {
            return ClassUtil.newInstance(type);
        }
        if (type.isInstance(data)) {
            return type.cast(data);
        }
        if (type == String.class) {
            return (T) mapper.writeValueAsString(data);
        }
        return mapper.convertValue(data, type);
    }

    @Override
    public String fromXml(String xmlInput) {
        if (xmlInput == null) return null;
        return XML.toJSONObject(xmlInput).toString();
    }

    @SneakyThrows
    @Override
    public <T> T fromXml(String xmlInput, String root, Class<T> kind) {
        if (xmlInput == null) {
            return ClassUtil.newInstance(kind);
        }
        JSONObject object = XML.toJSONObject(xmlInput);
        return deserialize(object.getJSONObject(root).toString(), kind);
    }

    @SneakyThrows
    @Override
    public String serialize(Object data) {
        if (data == null) return null;
        return mapper.writeValueAsString(data);
    }

    @SneakyThrows
    @Override
    public byte[] serializeAsBytes(Object data) {
        if (data == null) return null;
        return mapper.writeValueAsBytes(data);
    }

    @SneakyThrows
    @Override
    public <T> T deserialize(String data, Class<T> type) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return mapper.readValue(data, type);
    }

    @SneakyThrows
    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null) {
            return null;
        }
        return mapper.readValue(data, type);
    }

    @SneakyThrows
    @Override
    public <T> T deserialize(InputStream source, Class<T> type) {
        if (source == null) {
            return null;
        }
        return mapper.readValue(source, type);
    }

    @SneakyThrows
    @Override
    public String prettyPrint(Object data) {
        if (data == null) return null;
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    }

    @Override
    @SneakyThrows
    public void serializeToFile(Object content, File file) {
        if (content != null) {
            FileUtils.writeStringToFile(file, serialize(content), StandardCharsets.UTF_8);
        }
    }

    @Override
    @SneakyThrows
    public <T> Map<String, T> deserializeMap(String input) {
        if (input == null) {
            new HashMap<>();
        }
        MapLikeType mapType = mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);
        return mapper.readValue(input, mapType);
    }

    @Override
    @SneakyThrows
    public <T> Map<String, T> deserializeMap(InputStream input) {
        if (input == null) {
            new HashMap<>();
        }
        MapLikeType mapType = mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);
        return mapper.readValue(input, mapType);
    }

    @Override
    @SneakyThrows
    public <T> Map<String, T> deserializeMap(InputStream input, Class<T> type) {
        if (input == null) {
            return new HashMap<>();
        }
        MapLikeType mapType = mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, type);
        return mapper.readValue(input, mapType);
    }

    @Override
    @SneakyThrows
    public <T> Map<String, T> deserializeMap(String input, Class<T> type) {
        if (input == null) {
            return new HashMap<>();
        }
        MapLikeType mapType = mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, type);
        return mapper.readValue(input, mapType);
    }

    @Override
    @SneakyThrows
    public <T> T deserializeParametricType(String input, Class<?> rawType, Class<?>... parameterClasses) {
        if (input == null) {
            return null;
        }
        JavaType type = mapper.getTypeFactory().constructParametricType(rawType, parameterClasses);
        return mapper.readValue(input, type);
    }

    @Override
    @SneakyThrows
    public <T> List<T> deserializeList(String input, Class<T> type) {
        if (StringUtils.isBlank(input)) {
            return new ArrayList<>();
        }
        ArrayType mapType = mapper.getTypeFactory().constructArrayType(type);
        return Arrays.asList(mapper.readValue(input, mapType));
    }

    @Override
    @SneakyThrows
    public <T> List<T> deserializeList(InputStream input, Class<T> type) {
        if (input == null) {
            return new ArrayList<>();
        }
        ArrayType mapType = mapper.getTypeFactory().constructArrayType(type);
        return Arrays.asList(mapper.readValue(input, mapType));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Map<String, E> toMap(Object input, Class<E> valueClass) {
        return toMap(mapper, input, valueClass);
    }

    @SuppressWarnings("unchecked")
    public static <E> Map<String, E> toMap(ObjectMapper mapper, Object input, Class<E> valueClass) {
        if (input == null) {
            return new HashMap<>();
        }
        if (input instanceof Map) {
            return (Map<String, E>) input;
        }
        MapLikeType type = mapper.getTypeFactory().constructMapLikeType(Map.class, String.class, valueClass);
        if (input instanceof String) {
            try {
                return mapper.readValue((String) input, type);
            } catch (IOException e) {
                return new HashMap<>();
            }
        } else {
            return mapper.convertValue(input, type);
        }
    }
}
