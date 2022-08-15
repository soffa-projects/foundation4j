package dev.soffa.foundation.config;

import dev.soffa.foundation.error.NotImplementedException;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public class SimpleConfigManager implements ConfigManager {

    private Map<String, Object> properties;

    @Override
    public <T> T bind(String prefix, Class<T> kind) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public String require(String name) {
        return (String) Objects.requireNonNull(properties.get(name));
    }

}
