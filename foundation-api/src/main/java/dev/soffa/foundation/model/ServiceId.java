package dev.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class ServiceId {

    @JsonValue
    String value;

    @JsonCreator
    public ServiceId(String value) {
        this.value = value;
    }
}
