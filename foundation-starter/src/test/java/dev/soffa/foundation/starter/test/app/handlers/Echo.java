package dev.soffa.foundation.starter.test.app.handlers;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.Query;
import dev.soffa.foundation.core.Recorded;
import dev.soffa.foundation.starter.test.app.model.EchoInput;
import dev.soffa.foundation.starter.test.app.model.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
public class Echo implements Query<EchoInput, Message>, Recorded {

    @Override
    public Message handle(EchoInput input, @NonNull OperationContext ctx) {

        return new Message("echo", input.getMessage());
    }
}
