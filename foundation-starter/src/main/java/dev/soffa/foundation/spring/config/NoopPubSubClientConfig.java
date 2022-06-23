package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.spring.service.NoopPubSubClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoopPubSubClientConfig {

    @ConditionalOnMissingBean(PubSubClient.class)
    @Bean
    public PubSubClient createDefaultPubSubClient() {
        return new NoopPubSubClient();
    }
}
