package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.ApplicationSettingTemplate;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Optional;

@ConditionalOnBean(VaultTemplate.class)
@Component
@AllArgsConstructor
public class VaultApplicationSettingTemplateImpl implements ApplicationSettingTemplate {

    private final VaultTemplate vault;

    @Override
    public Optional<Map<String, Object>> get(String path) {
        try {
            VaultResponse response = vault.read(path);
            return Optional.of(response.getData());
        }catch (Exception e) {
            Logger.app.error(e);
        }
        return Optional.empty();
    }

}
