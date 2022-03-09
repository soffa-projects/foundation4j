package dev.soffa.foundation.messages;


import java.util.Optional;

public interface MessageHandler {

    Optional<Object> handle(Message message);

}
