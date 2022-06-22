package dev.soffa.foundation.error;

public class ForbiddenException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException() {
        this("RESOURCE_ACCESS_DEFINED");
    }

    public ForbiddenException(String message, Object... args) {
        super(message, args);
    }

    public ForbiddenException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
