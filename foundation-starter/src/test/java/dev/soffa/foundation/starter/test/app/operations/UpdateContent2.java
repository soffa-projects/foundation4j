package dev.soffa.foundation.starter.test.app.operations;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.starter.test.app.models.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UpdateContent2 implements Operation<String, Message> {

    @Override
    public Message handle(String id, @NonNull Context ctx) {
        return new Message(id);
    }
}
