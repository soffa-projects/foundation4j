package dev.soffa.foundation.starter.test.app.handlers;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.starter.test.app.model.Message;
import dev.soffa.foundation.starter.test.app.model.UpdateContentInput;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UpdateContent implements Operation<UpdateContentInput, Message> {

    @Override
    public Message handle(UpdateContentInput input, @NonNull OperationContext ctx) {
        return new Message("echo.update", input.getId() + "/" + input.getContent());
    }
}
