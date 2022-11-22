package dev.soffa.foundation.config;

import dev.soffa.foundation.commons.Mappers;

import java.util.Map;
import java.util.Optional;

public interface ApplicationSettingTemplate {

    default <T> Optional<T> get(String path, Class<T> kind) {
        return this.get(path).map(o -> Mappers.JSON_DEFAULT.convert(o, kind));
    }

    Optional<Map<String, Object>> get(String path);

}
