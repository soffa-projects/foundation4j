package dev.soffa.foundation.model;

import lombok.Data;

@Data
public class Event {
    private String operation;
    private String target = "*";
    private String payload;
}
