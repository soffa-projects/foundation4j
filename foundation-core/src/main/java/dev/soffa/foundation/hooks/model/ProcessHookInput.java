package dev.soffa.foundation.hooks.model;

import dev.soffa.foundation.commons.Mappers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ProcessHookInput {

    private String operationId;
    private String eventId;
    private String data;

    public static ProcessHookInput create(String operationId) {
        return create(operationId, null, null);
    }

    public static ProcessHookInput create(String operationId, Map<String, Object> data) {
        return create(operationId, null, data);
    }

    public static ProcessHookInput create(String operationId, String eventId, Map<String, Object> data) {
        if (data == null) {
            return new ProcessHookInput(operationId, eventId, null);
        } else {
            return new ProcessHookInput(operationId, eventId, Mappers.JSON_FULLACCESS.serialize(data));
        }
    }

}
