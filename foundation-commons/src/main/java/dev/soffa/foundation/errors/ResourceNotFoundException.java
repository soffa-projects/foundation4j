package dev.soffa.foundation.errors;

public class ResourceNotFoundException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message, Object... args) {
        super(message, args);
    }

    public ResourceNotFoundException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
