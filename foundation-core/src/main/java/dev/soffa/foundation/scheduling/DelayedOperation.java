package dev.soffa.foundation.scheduling;

import dev.soffa.foundation.core.model.Serialized;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelayedOperation<I> {

    private String uuid;
    private String operation;
    private Serialized input;
}
