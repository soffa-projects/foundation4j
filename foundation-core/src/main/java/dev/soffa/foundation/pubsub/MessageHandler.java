package dev.soffa.foundation.pubsub;


import dev.soffa.foundation.messages.Message;

import java.util.Optional;

public interface MessageHandler {

    Optional<Object> handle(Message message);

}
