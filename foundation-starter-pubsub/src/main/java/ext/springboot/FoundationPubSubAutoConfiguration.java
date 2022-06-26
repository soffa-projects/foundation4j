package ext.springboot;

import dev.soffa.foundation.commons.EventBus;
import dev.soffa.foundation.events.ApplicationStartedEvent;
import dev.soffa.foundation.events.OnServiceStarted;
import dev.soffa.foundation.message.DispatchMessageHandler;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubConfig;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.model.ServiceId;
import dev.soffa.foundation.pubsub.PubSubMessengerFactory;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
@PropertySource("classpath:application-foundation-pubsub.properties")
public class FoundationPubSubAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "app.pubsub")
    public PubSubConfig createPubSubConfig() {
        return new PubSubConfig();
    }

    @Bean
    @Primary
    public PubSubMessenger createPubSubMessenger(@Value("${spring.application.name}") String applicationName,
                                                 PubSubConfig config,
                                                 DispatchMessageHandler handler) {
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
            Message msg = MessageFactory.create(OnServiceStarted.class.getSimpleName(), new ServiceId(serviceId));
            messenger.broadcast(msg);
            EventBus.post(new ApplicationStartedEvent());
        }
    }


}
