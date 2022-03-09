package dev.soffa.foundation.commons;

import java.util.HashMap;
import java.util.Map;

public final class MapUtil {

    private MapUtil() {
    }

    public static Map<String, Object> create(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("MapUtil.create() requires an even number of arguments");
        }
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("MapUtil.create() requires String keys");
            }
            result.put(args[i].toString(), args[i + 1]);
        }
        return result;
    }

    public static boolean isEmpty(Map<String, Object> tags) {
        return tags == null || tags.isEmpty();
    }

    public static boolean isNotEmpty(Map<String, Object> tags) {
        return !isEmpty(tags);
    }
}
