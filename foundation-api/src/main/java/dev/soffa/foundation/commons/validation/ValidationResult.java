package dev.soffa.foundation.commons.validation;

import dev.soffa.foundation.error.ValidationException;

import java.util.Map;

public interface ValidationResult {

    Map<String,String> getErrors();
    boolean hasErrors();
    void thowAnyError() throws ValidationException;

}
