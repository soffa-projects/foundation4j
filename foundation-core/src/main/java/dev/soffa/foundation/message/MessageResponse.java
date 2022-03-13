package dev.soffa.foundation.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.error.ErrorUtil;
import lombok.Data;

@Data
public class MessageResponse {

    private int errorCode;
    private String error;
    private byte[] data;

    @JsonIgnore
    public boolean hasError() {
        return error != null;
    }

    public boolean isSuccess() {
        return !hasError();
    }

    public static MessageResponse error(Exception e) {
        return of(null, e);
    }

    public static MessageResponse ok(Object payload) {
        return of(payload, null);
    }

    public static MessageResponse of(Object payload, Exception e) {
        MessageResponse response = new MessageResponse();
        if (e != null) {
            response.setErrorCode(ErrorUtil.resolveErrorCode(e));
            response.setError(e.getMessage());
        }
        if (payload != null) {
            response.setData(Mappers.JSON.serializeAsBytes(payload));
        }
        return response;
    }

}
