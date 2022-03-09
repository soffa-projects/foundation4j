package dev.soffa.foundation.errors;

public class InvalidAuthException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public InvalidAuthException(String message, Object... args) {
        super(message, args);
    }

    public InvalidAuthException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public InvalidAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
