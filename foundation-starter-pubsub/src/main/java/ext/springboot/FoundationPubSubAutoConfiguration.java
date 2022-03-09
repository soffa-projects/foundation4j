package ext.springboot;

import dev.soffa.foundation.events.OnServiceStarted;
import dev.soffa.foundation.messages.Message;
import dev.soffa.foundation.messages.MessageFactory;
import dev.soffa.foundation.messages.pubsub.MessageHandler;
import dev.soffa.foundation.messages.pubsub.PubSubConfig;
import dev.soffa.foundation.messages.pubsub.PubSubMessenger;
import dev.soffa.foundation.models.ServiceInfo;
import dev.soffa.foundation.pubsub.PubSubMessengerFactory;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
@ConditionalOnProperty(name = "app.pubsub.enabled", havingValue = "true", matchIfMissing = true)
public class FoundationPubSubAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "app.pubsub")
    public PubSubConfig createNatsConfig() {
        return new PubSubConfig();
    }

    @Bean
    @Primary
    public PubSubMessenger createPubSubMessenger(@Value("${spring.application.name}") String applicationName,
                                                 PubSubConfig config,
                                                 @Autowired(required = false) MessageHandler handler) {
        PubSubMessenger messenger = PubSubMessengerFactory.create(applicationName, config, handler);
        messenger.afterPropertiesSet();
        return messenger;
    }

    @Bean
    public ServiceReadinessNotifier createServiceReadyNotifier(@Value("${spring.application.name}") String applicationName, PubSubMessenger messenger) {
        return new ServiceReadinessNotifier(applicationName, messenger);
    }

    @AllArgsConstructor
    private static class ServiceReadinessNotifier implements ApplicationListener<ContextRefreshedEvent> {

        private final String serviceId;
        private final PubSubMessenger messenger;

        @Override
        public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
            Message msg = MessageFactory.create(OnServiceStarted.class.getSimpleName(), new ServiceInfo(serviceId));
            messenger.broadcast("*", msg);
        }
    }


}
