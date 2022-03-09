package dev.soffa.foundation.mail;


import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.mail.models.EmailAck;

public interface EmailSender {

    EmailAck send(Email message);

}
