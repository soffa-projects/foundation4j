package dev.soffa.foundation.context;


import java.util.Properties;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class Environment {

    private static final Properties PROPS = new Properties();

    private Environment() {
    }

    public static void set(String key, Object value) {
        PROPS.put(key, value);
    }

    public static boolean containsKey(String key) {
        return PROPS.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) PROPS.getProperty(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        T res = (T) PROPS.get(key);
        if (res == null) {
            return defaultValue;
        }
        return res;
    }


}
