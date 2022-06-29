package dev.soffa.foundation.starter.test.app.handlers;

import dev.soffa.foundation.annotation.Publish;
import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.Broadcast;
import dev.soffa.foundation.starter.test.app.model.EchoInput;
import dev.soffa.foundation.starter.test.app.model.Message;
import dev.soffa.foundation.starter.test.app.operation.EchoSecured;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Named;

@Named
public class DoEchoSecured implements EchoSecured, Broadcast {

    @Override
    @Publish(event = "TENANT_CREATED", target = "*")
    public Message handle(EchoInput input, @NonNull OperationContext ctx) {
        return new Message("echo.secure", input.getMessage());
    }
}
