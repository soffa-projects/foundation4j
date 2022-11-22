package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.ApplicationSettingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class ExtBeansFactory {

    @Bean
    @Primary
    @ConditionalOnProperty(value = "spring.cloud.vault.enabled", havingValue = "true")
    public ApplicationSettingTemplate createVaultApplicationSettingTemplate(@Autowired VaultTemplate vault,
                                                                            @Value("${spring.cloud.vault.kv.backend}") String backend) {

        Logger.app.info("Using VaultApplicationSettingTemplate with backend: %s", backend);
        return new VaultApplicationSettingTemplate(vault, backend);
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationSettingTemplate.class)
    public ApplicationSettingTemplate createDefaultApplicationSettingTemplate() {
        Logger.app.warn("No ApplicationSettingTemplate configured");
        return new NoApplicationSettingTemplate();
    }


}
