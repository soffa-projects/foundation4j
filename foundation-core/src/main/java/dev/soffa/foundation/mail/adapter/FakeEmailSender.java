package dev.soffa.foundation.mail.adapter;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.RandomUtil;
import dev.soffa.foundation.mail.EmailSender;
import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.mail.models.EmailAck;

import java.util.concurrent.atomic.AtomicInteger;

public class FakeEmailSender implements EmailSender {

    private static final Logger LOG = Logger.get(FakeEmailSender.class);
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static int getCounter() {
        return COUNTER.get();
    }

    @Override
    public EmailAck send(Email message) {
        LOG.info(
            "Email processed by FakeEmailSender:\nFrom: %s\nSubject: %s\nTo: %s",
            message.getSender(), message.getSubject(), Mappers.JSON_DEFAULT.serialize(message.getTo())
        );
        COUNTER.incrementAndGet();
        return new EmailAck("OK", RandomUtil.nextString());
    }
}
