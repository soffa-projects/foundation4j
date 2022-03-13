package dev.soffa.foundation.starter.test.app.operation;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.starter.test.app.model.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
public class Echo implements Operation<EchoInput, Message> {

    @Override
    public Message handle(EchoInput input, @NonNull Context ctx) {
        return new Message(input.getMessage());
    }
}
