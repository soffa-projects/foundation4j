package dev.soffa.foundation.commons;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
public class Value {

    private Object obj;

    public Value(Object value) {
        this.obj = value;
    }

    public static <T> T getOrElse(T value, T defaultValue) {
        if (value instanceof String) {
            if (TextUtil.isEmpty((String) value)) {
                return defaultValue;
            }
            return value;
        }
        return Optional.ofNullable(value).orElse(defaultValue);
    }

    public boolean isNull() {
        return obj == null;
    }

}
