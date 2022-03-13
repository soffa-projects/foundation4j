package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.multitenancy.PubSubTenantsLoader;
import dev.soffa.foundation.multitenancy.TenantsLoader;
import dev.soffa.foundation.security.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "app.tenants-provider.pubsub.enabled", havingValue = "true")
public class PubSubTenantsLoaderConfig {

    @Bean
    @ConditionalOnMissingBean(TenantsLoader.class)
    public TenantsLoader createPubSubTenantsLoader(PubSubMessenger client,
                                                   TokenProvider tokens,
                                                   AppConfig app,
                                                   @Value("${app.tenants-provider.pubsub.subject}") String serviceId,
                                                   @Value("${app.tenants-provider.pubsub.token-permission:service}") String permission) {
        return new PubSubTenantsLoader(client, tokens, app, serviceId, permission);
    }
}
