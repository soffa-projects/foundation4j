package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.config.ApplicationSettingTemplate;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class NoApplicationSettingTemplate implements ApplicationSettingTemplate {

    @Override
    public Optional<Map<String, Object>> get(String path) {
        return Optional.empty();
    }

}
