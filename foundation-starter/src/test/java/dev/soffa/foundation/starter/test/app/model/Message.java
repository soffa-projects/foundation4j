package dev.soffa.foundation.starter.test.app.model;

import dev.soffa.foundation.annotation.Hateos;
import lombok.Value;

@Value
@Hateos
public class Message {

    String messageId;
    String content;

}
