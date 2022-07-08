package dev.soffa.foundation.commons;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class CollectionUtil {

    private CollectionUtil() {
    }

    public static boolean isNotEmpty(Collection<?> list) {
        return !isEmpty(list);
    }


    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(Set<?> set) {
        return !isEmpty(set);
    }

    public static boolean isEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Iterator<?> array) {
        return array!=null && array.hasNext();
    }
    public static boolean isNotEmpty(Object... array) {
        return array != null && array.length > 0;
    }
}
