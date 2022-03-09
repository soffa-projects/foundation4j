package dev.soffa.foundation.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class Regex {

    @JsonValue
    String value;

    @JsonCreator
    public Regex(String value) {
        this.value = value;
    }
}
