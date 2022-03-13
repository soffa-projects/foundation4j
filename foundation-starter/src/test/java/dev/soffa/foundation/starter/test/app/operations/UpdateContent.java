package dev.soffa.foundation.starter.test.app.operations;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.models.ResponseEntity;
import dev.soffa.foundation.starter.test.app.models.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UpdateContent implements Operation<UpdateContentInput, ResponseEntity<Message>> {

    @Override
    public ResponseEntity<Message> handle(UpdateContentInput input, @NonNull Context ctx) {
        return ResponseEntity.created(new Message(input.getId() + "/" + input.getContent()));
    }
}
