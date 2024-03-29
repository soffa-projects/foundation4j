package dev.soffa.foundation.error;


import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ValidationException extends Exception implements ManagedException {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;

    private final String message;

    public ValidationException(String message) {
        super(message);
        this.message = message;
        errors = ImmutableMap.of();
    }

    public ValidationException(Map<String, String> errors) {
        this("Validation's failed", errors);
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
        this.message = message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
