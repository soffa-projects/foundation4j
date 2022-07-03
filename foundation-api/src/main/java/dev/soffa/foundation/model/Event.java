package dev.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String operation;
    private String target = "*";
    private String payload;

    public Event(String operation, String payload) {
        this.operation = operation;
        this.payload = payload;
    }
}


