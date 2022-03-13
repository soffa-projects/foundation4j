package dev.soffa.foundation.mail.adapter;

import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.error.ConfigurationException;
import dev.soffa.foundation.error.NotImplementedException;
import dev.soffa.foundation.mail.EmailSender;
import dev.soffa.foundation.mail.models.MailerConfig;

import java.util.Objects;

public final class EmailSenderFactory {

    private EmailSenderFactory() {
    }

    public static EmailSender create(String url, String defaultSender) {
        UrlInfo uri = UrlInfo.parse(url);
        String lDefaultSender = defaultSender;
        if (TextUtil.isEmpty(lDefaultSender)) {
            lDefaultSender = uri.param("from").orElse(null);
        }

        if ("smtp".equalsIgnoreCase(uri.getProtocol())) {
            MailerConfig config = new MailerConfig();
            config.setSender(lDefaultSender);
            config.setHostname(uri.getHostname());
            config.setPort(uri.getPort());
            boolean hasTlS = Objects.equals(uri.param("tls", "enabled"), "disabled");
            config.setTls(hasTlS);
            return new SmtpEmailSender(config);

        } else if ("faker".equalsIgnoreCase(uri.getProtocol())) {

            return new FakeEmailSender();

        } else if ("sendgrid".equalsIgnoreCase(uri.getProtocol())) {

            String apiKey = uri.getUsername();
            if (TextUtil.isEmpty(apiKey)) {
                apiKey = uri.param("apiKey").orElse(null);
            }
            if (TextUtil.isEmpty(apiKey)) {
                throw new ConfigurationException("Unable to locate Sendgrid apiKey");
            }
            return new SendgridEmailSender(apiKey, lDefaultSender);
        }

        throw new NotImplementedException("Protocol not supported: " + uri.getProtocol());
    }

}
