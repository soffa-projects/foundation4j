package dev.soffa.foundation.error;

public class RetryException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public RetryException(String message, Object... args) {
        super(message, args);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
