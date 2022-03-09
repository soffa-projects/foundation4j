package dev.soffa.foundation.commons.http;

import java.util.Map;
import java.util.Optional;

public class HttpContextHolder {

    private static final ThreadLocal<Map<String, String>> CURRENT = new InheritableThreadLocal<>();

    public static void set(Map<String, String> value) {
        if (value == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(value);
        }
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static boolean isEmpty() {
        return CURRENT.get() == null;
    }

    public static Optional<Map<String, String>> get() {
        return Optional.ofNullable(CURRENT.get());
    }

}
