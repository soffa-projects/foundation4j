package dev.soffa.foundation.spring.handler;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.action.PublishEvent;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import dev.soffa.foundation.model.Event;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(PubSubMessenger.class)
@AllArgsConstructor
public class DoPublishEvent implements PublishEvent {

    private final PubSubMessenger messenger;

    @Override
    public Void handle(Event input, @NonNull OperationContext ctx) {
        messenger.publish(input.getTarget(), MessageFactory.create(input.getOperation(), input.getPayload()));
        return null;
    }
}
