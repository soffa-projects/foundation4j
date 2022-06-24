package dev.soffa.foundation.mail;


import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.mail.models.EmailAck;
import dev.soffa.foundation.model.EmailAddress;

import java.util.Collections;

public interface EmailSender {

    EmailAck send(Email message);

    default EmailAck send(EmailAddress to, String suject, String message) {
        return send(Email.builder().to(Collections.singletonList(to)).subject(suject).htmlMessage(message).build());
    }

}
