package dev.soffa.foundation.pubsub.spring;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.action.PublishEvent;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.model.Event;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DoPublishEvent implements PublishEvent {

    private final PubSubMessenger messenger;

    @Override
    public Void handle(Event input, @NonNull OperationContext ctx) {
        messenger.publish(input.getTarget(), MessageFactory.create(input.getOperation(), input.getPayload()));
        return null;
    }
}
