package dev.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class NumericValue {
    private final Object value;

    @JsonCreator
    public NumericValue(long value) {
        this.value = value;
    }

    @JsonCreator
    public NumericValue(double value) {
        this.value = value;
    }

    @JsonCreator
    public NumericValue(float value) {
        this.value = value;
    }

    @JsonCreator
    public NumericValue(int value) {
        this.value = value;
    }

    @JsonValue
    public Object getValue() {
        return value;
    }

}
