package dev.soffa.foundation.model;

import lombok.Value;

@Value
public class Event {
    String name;
    String target = "*";
    Object payload;
}
