package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
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
        if (TextUtil.isNotEmpty(backend) && !p.startsWith(backend)) {
            p = backend + (p.startsWith("/") ? "" : "/") + p;
        }
        try {
            VaultResponse response = vault.read(p);
            if (response == null) {
                Logger.platform.warn("No secret found @ %s", p);
                return Optional.empty();
            }
            Map<String, Object> data = response.getData();
            if (data == null) {
                Logger.platform.warn("No secret found @ %s", p);
            }else {
                Logger.app.info("Secret retrieved @ %s", p);
            }
            return Optional.ofNullable(data);
        } catch (Exception e) {
            Logger.app.error(e);
        }
        return Optional.empty();
    }


}
