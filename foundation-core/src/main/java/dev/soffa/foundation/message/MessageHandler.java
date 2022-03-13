package dev.soffa.foundation.message;


import java.util.Optional;

public interface MessageHandler {

    Optional<Object> handle(Message message);

}
