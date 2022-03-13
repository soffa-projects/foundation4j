package dev.soffa.foundation.error;

public class NoContentException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public NoContentException(String message, Object... args) {
        super(message, args);
    }

    public NoContentException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public NoContentException(String message, Throwable cause) {
        super(message, cause);
    }

}
