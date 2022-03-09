package dev.soffa.foundation.extras.mail;


import dev.soffa.foundation.extras.mail.models.Email;
import dev.soffa.foundation.extras.mail.models.EmailAck;

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
        return defaultSender.send(message);
    }
}
