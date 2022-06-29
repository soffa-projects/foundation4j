package dev.soffa.foundation.model;

import lombok.Value;

@Value
public class Event {
    String operation;
    String target = "*";
    String payload;
}
