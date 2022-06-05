package ext.springboot;

import dev.soffa.foundation.mail.EmailSender;
import dev.soffa.foundation.mail.Mailer;
import dev.soffa.foundation.support.mail.EmailSenderFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EmailAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "app.mail")
    public EmailConfig createSmtpConfig() {
        return new EmailConfig();
    }

    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    public Mailer createEmailSender(EmailConfig config) {
        Map<String, EmailSender> clients = new HashMap<>();
        if (config.getClients() != null) {
            for (Map.Entry<String, String> e : config.getClients().entrySet()) {
                clients.put(e.getKey(), EmailSenderFactory.create(e.getValue(), config.getFrom()));
            }
        }
        return new Mailer(clients);
    }

}
