package dev.soffa.foundation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.soffa.foundation.commons.ObjectUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.errors.ErrorUtil;
import lombok.Data;

@Data
public class CallResult {

    private Integer errorCode;
    private String error;
    private byte[] data;

    public static CallResult create(Object payload, Exception e) {
        CallResult response = new CallResult();
        if (e != null) {
            response.setErrorCode(ErrorUtil.resolveErrorCode(e));
            response.setError(e.getMessage());
        }
        response.setData(ObjectUtil.serialize(payload));
        return response;
    }

    @JsonIgnore
    public boolean hasError() {
        return TextUtil.isNotEmpty(error);
    }

    public boolean isSuccess() {
        return !hasError();
    }

}
