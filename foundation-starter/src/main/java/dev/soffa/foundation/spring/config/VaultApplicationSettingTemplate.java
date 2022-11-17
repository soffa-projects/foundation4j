package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.ApplicationSettingTemplate;
import lombok.AllArgsConstructor;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class VaultApplicationSettingTemplate implements ApplicationSettingTemplate {

    private final VaultTemplate vault;
    private final String backend;

    @Override
    public Optional<Map<String, Object>> get(String path) {
        String p = path;
        if (p.startsWith("backend://")) {
            p = p.replace("backend://", backend);
        }
        try {
            VaultResponse response = vault.read(p);
            if (response==null) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.getData());
        }catch (Exception e) {
            Logger.app.error(e);
        }
        return Optional.empty();
    }


}
