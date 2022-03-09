package dev.soffa.foundation.spring.config;

import com.google.common.base.CaseFormat;
import dev.soffa.foundation.commons.MapUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.metrics.MetricsRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@AllArgsConstructor
public class MetricsRegistryImpl implements MetricsRegistry {

    public static final String GLOBAL = "_global";
    private final MeterRegistry registry;

    @Override
    public void increment(String name, double amount, Map<String, Object> tags) {
        final String lName = normalize(name);
        registry.counter(lName + GLOBAL).increment(amount);
        if (MapUtil.isNotEmpty(tags)) {
            registry.counter(lName).increment(amount);
        }
    }

    @Override
    public double globalCounter(String name) {
        return counter(name + GLOBAL);
    }

    @Override
    public double counter(String name) {
        try {
            return registry.get(normalize(name)).counter().count();
        } catch (MeterNotFoundException e) {
            return 0;
        }
    }

    @Override
    public void timed(String name, Duration duration, Map<String, Object> tags) {
        final String lName = normalize(name);
        registry.timer(lName + GLOBAL).record(duration);
        if (MapUtil.isNotEmpty(tags)) {
            registry.timer(lName, createTags(tags)).record(duration);
        }
    }

    @Override
    public void timed(String name, Map<String, Object> tags, Runnable runnable) {
        final String lName = normalize(name);
        registry.timer(lName + GLOBAL).record(() -> {
            if (MapUtil.isEmpty(tags)) {
                runnable.run();
            } else {
                registry.timer(lName, createTags(tags)).record(runnable);
            }
        });

    }

    @Override
    public <F> F timed(String name, Map<String, Object> tags, Supplier<F> supplier) {
        final String lName = normalize(name);
        return registry.timer(normalize(lName + GLOBAL)).record(() -> {
            if (MapUtil.isEmpty(tags)) {
                return supplier.get();
            }
            return registry.timer(lName, createTags(tags)).record(supplier);
        });
    }

    private String[] createTags(Map<String, Object> tags) {
        List<String> r = new ArrayList<>();
        for (Map.Entry<String, Object> e : tags.entrySet()) {
            if (TextUtil.isNotEmpty(e.getKey()) && e.getValue() != null) {
                Object value = e.getValue();
                if (value instanceof Optional) {
                    Optional<?> opt = (Optional<?>) value;
                    if (opt.isPresent()) {
                        value = opt.get();
                    } else {
                        continue;
                    }
                }
                String svalue = value.toString();
                if (TextUtil.isNotEmpty(svalue)) {
                    r.add(e.getKey());
                    r.add(svalue);
                }
            }
        }
        return r.toArray(new String[0]);
    }


    private String normalize(@NonNull String input) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, input).replaceAll("_+", "_");
    }
}
