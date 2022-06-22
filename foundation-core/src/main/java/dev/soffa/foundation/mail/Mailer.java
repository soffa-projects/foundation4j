package dev.soffa.foundation.mail;


import com.google.common.base.Preconditions;
import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.mail.models.EmailAck;

import java.util.Map;

public class Mailer implements EmailSender {

    public static final String DEFAULT = "default";
    private final Map<String, EmailSender> clients;
    private EmailSender defaultSender;

    public Mailer(Map<String, EmailSender> clients) {
        this.clients = clients;
        if (clients != null && !clients.isEmpty()) {
            defaultSender = clients.get(DEFAULT);
            if (defaultSender == null) {
                defaultSender = clients.values().iterator().next();
            }
        }
    }

    public EmailSender getClient(String id) {
        return clients.get(id);
    }

    @Override
    public EmailAck send(Email message) {
        Preconditions.checkNotNull(defaultSender, "No email sender defined");
        return defaultSender.send(message);
    }
}
