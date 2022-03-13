package dev.soffa.foundation.error;

public class InvalidTokenException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public InvalidTokenException(String message, Object... args) {
        super(message, args);
    }

    public InvalidTokenException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

}
