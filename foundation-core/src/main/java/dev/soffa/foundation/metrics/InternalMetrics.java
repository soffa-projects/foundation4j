package dev.soffa.foundation.metrics;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InternalMetrics {

    private static final Map<String, AtomicLong> COUNTERS = new ConcurrentHashMap<>();

    public static long increment(String prefix, String counter) {
        return increment(prefix + "." + counter);
    }

    public static long increment(String counter) {
        return COUNTERS.computeIfAbsent(normalizeName(counter), s -> new AtomicLong()).incrementAndGet();
    }

    public static long getCounter(String prefix, String counter) {
        return getCounter(prefix + "." + counter);
    }

    public static long getCounter(String counter) {
        return COUNTERS.computeIfAbsent(normalizeName(counter), s -> new AtomicLong()).get();
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
