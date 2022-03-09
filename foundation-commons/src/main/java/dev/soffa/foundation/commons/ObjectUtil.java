package dev.soffa.foundation.commons;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Optional;


public final class ObjectUtil {


    private ObjectUtil() {
    }

    @SneakyThrows
    private static byte[] serialize(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof byte[]) {
            return (byte[]) input;
        }
        if (input instanceof Optional) {
            Optional<?> opt = (Optional<?>) input;
            return opt.map(ObjectUtil::serialize).orElse(null);
        }
        return JsonStream.serialize(input).getBytes(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private static <T> T deserialize(byte[] input, Class<T> type) {
        if (input == null || input.length == 0) {
            return null;
        }
        return JsonIterator.deserialize(input, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T input) {
        return (T) deserialize(serialize(input), input.getClass());
    }

}
