package dev.soffa.foundation.commons;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Properties {

    private final Map<String, String> internal;

    public Properties(Map<String, String> internal) {
        this.internal = internal;
    }

    public Properties() {
        this(new ConcurrentHashMap<>());
    }

    public boolean has(String name) {
        return internal.containsKey(name);
    }

    public String get(String name, String defaultValue) {
        if (!has(name)) {
            return defaultValue;
        }
        return internal.get(name);
    }

    public int getInt(String name, int defaultValue) {
        if (!has(name)) {
            return defaultValue;
        }
        return Integer.parseInt(internal.get(name));
    }
}
