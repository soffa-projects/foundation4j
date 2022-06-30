package dev.soffa.foundation.extra.jobs;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import dev.soffa.foundation.model.VO;
import lombok.Value;

@Value
public class PendingJobId implements VO {

    private static final long serialVersionUID = 1L;
    String value;

    @JsonCreator
    public PendingJobId(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
