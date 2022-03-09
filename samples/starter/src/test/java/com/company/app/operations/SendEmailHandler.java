package com.company.app.operations;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.extras.mail.models.Email;
import dev.soffa.foundation.extras.mail.models.EmailAck;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SendEmailHandler implements SendEmail {

    public static final AtomicLong COUNTER = new AtomicLong(0);

    @Override
    public EmailAck handle(@NonNull Email input, @NonNull Context ctx) {
        COUNTER.incrementAndGet();
        return new EmailAck("OK", "000");
    }
}
