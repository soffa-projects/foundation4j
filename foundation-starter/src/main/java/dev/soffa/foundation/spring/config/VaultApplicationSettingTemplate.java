package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.ApplicationSettingTemplate;
import lombok.AllArgsConstructor;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class VaultApplicationSettingTemplate implements ApplicationSettingTemplate {

    private final VaultTemplate kv;
    private final String backend;

    @Override
    public Optional<Map<String, Object>> get(String path) {
        try {
            VaultResponse response = kv.opsForKeyValue(this.backend, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(path);
            if (response == null) {
                Logger.platform.warn("No secret found @ %s", path);
                return Optional.empty();
            }
            Map<String, Object> data = response.getData();
            if (data == null) {
                Logger.platform.warn("No secret found @ %s", path);
            }else {
                Logger.app.debug("Secret retrieved @ %s", path);
            }
            return Optional.ofNullable(data);
        } catch (Exception e) {
            Logger.app.error(e);
        }
        return Optional.empty();
    }


}
