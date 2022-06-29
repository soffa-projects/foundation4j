package dev.soffa.foundation.starter.test.app.handlers;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.starter.test.app.model.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UpdateContent2 implements Operation<@NonNull String, Message> {

    @Override
    public Message handle(@NonNull String id, @NonNull OperationContext ctx) {
        return new Message("udate2", id);
    }
}
