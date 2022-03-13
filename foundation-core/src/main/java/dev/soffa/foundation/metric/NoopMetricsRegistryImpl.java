package dev.soffa.foundation.metric;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class NoopMetricsRegistryImpl implements MetricsRegistry {

    private static final Map<String, Double> REG = new ConcurrentHashMap<>();

    @Override
    public void increment(String counter, double value, Map<String, Object> tags) {
        REG.put(counter, REG.getOrDefault(counter, 0d) + 1);
    }

    @Override
    public double counter(String name) {
        return 0;
    }

    @Override
    public double globalCounter(String name) {
        return 0;
    }

    @Override
    public void timed(String name, Duration duration, Map<String, Object> tags) {
        // Add a dummy implementation
    }

    @Override
    public void timed(String name, Map<String, Object> tags, Runnable runnable) {
        runnable.run();
    }

    @Override
    public <F> F timed(String name, Map<String, Object> tags, Supplier<F> supplier) {
        return supplier.get();
    }
}
