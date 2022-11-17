package dev.soffa.foundation.config;

import java.util.Map;
import java.util.Optional;

public interface ApplicationSettingTemplate {

    Optional<Map<String, Object>> get(String path);

}
