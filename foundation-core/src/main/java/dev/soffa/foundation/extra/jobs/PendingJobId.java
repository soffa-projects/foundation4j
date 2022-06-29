package dev.soffa.foundation.extra.jobs;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import dev.soffa.foundation.model.VO;
import lombok.Value;

@Value
public class PendingJobId implements VO {
    String value;

    @JsonCreator
    public PendingJobId(String value) {
        this.value = value;
    }

    @JsonValue
    public String toString() {
        return value;
    }
}
