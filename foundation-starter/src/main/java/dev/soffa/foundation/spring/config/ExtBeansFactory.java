package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.ApplicationSettingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultVersionedKeyValueTemplate;

@Configuration
public class ExtBeansFactory {

    @Bean
    @ConditionalOnMissingBean(ApplicationSettingTemplate.class)
    public ApplicationSettingTemplate createApplicationSettingTemplate(@Autowired(required = false) VaultVersionedKeyValueTemplate kv,
                                                                       @Value("${spring.cloud.vault.kv.backend:secret/}") String backend) {

        if (kv != null) {
            Logger.app.info("Using VaultApplicationSettingTemplate with backend: %s", backend);
            return new VaultApplicationSettingTemplate(kv, backend);
        } else {
            Logger.app.warn("No ApplicationSettingTemplate configured");
            return new NoApplicationSettingTemplate();
        }
    }


}
