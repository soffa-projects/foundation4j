package dev.soffa.foundation.hooks.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessHookInput {

    @NonNull
    private String operationId;
    private String eventId;
    private Map<String,Object> data;

}
