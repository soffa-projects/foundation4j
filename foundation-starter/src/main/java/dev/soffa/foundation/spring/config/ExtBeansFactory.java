package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.ApplicationSettingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class ExtBeansFactory {

    @Bean
    @ConditionalOnMissingBean(ApplicationSettingTemplate.class)
    public ApplicationSettingTemplate createApplicationSettingTemplate(@Autowired(required = false) VaultTemplate vault,
                                                                       @Value("${spring.cloud.vault.kv.backend:secret/}") String backend) {

        if (vault != null) {
            Logger.app.info("Using VaultApplicationSettingTemplate");
            return new VaultApplicationSettingTemplate(vault, backend);
        } else {
            Logger.platform.warn("No ApplicationSettingTemplate configured");
            return new NoApplicationSettingTemplate();
        }
    }


}
