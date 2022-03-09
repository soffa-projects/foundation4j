package dev.soffa.foundation.errors;

import java.text.MessageFormat;

public class ValidationException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    private String field;

    public ValidationException(String field, String message, Object... args) {
        super(message, args);
        this.field = field;
    }

    public ValidationException(String field, String message, Throwable cause, Object... args) {
        super(cause, message, args);
        this.field = field;
    }

    public ValidationException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ValidationException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getField() {
        return field;
    }
}
