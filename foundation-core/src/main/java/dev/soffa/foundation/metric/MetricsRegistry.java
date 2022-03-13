package dev.soffa.foundation.metric;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.error.ManagedException;
import dev.soffa.foundation.error.TechnicalException;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

public interface MetricsRegistry {

    String FAILED_SUFFIX = "_failed";
    String DURATION_SUFFIX = "_duration";
    Logger LOG = Logger.get(MetricsRegistry.class);

    default void increment(String counter) {
        increment(counter, 1, ImmutableMap.of());
    }

    default void increment(String counter, Map<String, Object> tags) {
        increment(counter, 1, tags);
    }

    default <T> T track(String prefix, Map<String, Object> tags, Supplier<T> supplier) {
        try {
            T result = timed(prefix + DURATION_SUFFIX, tags, supplier);
            increment(prefix, tags);
            return result;
        } catch (Exception e) {
            increment(prefix + FAILED_SUFFIX, tags);
            if (e instanceof ManagedException) {
                throw e;
            } else {
                throw new TechnicalException(e.getMessage(), e);
            }
        }
    }

    default void track(String prefix, Map<String, Object> tags, Runnable runnable) {
        try {
            timed(prefix + DURATION_SUFFIX, tags, runnable);
            increment(prefix, tags);
        } catch (Exception e) {
            increment(prefix + FAILED_SUFFIX, tags);
            if (e instanceof ManagedException) {
                throw e;
            } else {
                throw new TechnicalException(e.getMessage(), e);
            }
        }
    }

    void increment(String counter, double amount, Map<String, Object> tags);

    double counter(String name);

    double globalCounter(String name);

    void timed(String name, Duration duration, Map<String, Object> tags);

    void timed(String name, Map<String, Object> tags, Runnable runnable);

    <F> F timed(String name, Map<String, Object> tags, Supplier<F> supplier);
}
