package dev.soffa.foundation.extras.mail;


import dev.soffa.foundation.extras.mail.models.Email;
import dev.soffa.foundation.extras.mail.models.EmailAck;

public interface EmailSender {

    EmailAck send(Email message);

}
