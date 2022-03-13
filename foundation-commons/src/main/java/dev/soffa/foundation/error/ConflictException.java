package dev.soffa.foundation.error;

public class ConflictException extends FunctionalException {

    private static final long serialVersionUID = -8371753258676604024L;

    public ConflictException(String message, Object... args) {
        this(null, message, args);
    }

    public ConflictException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public ConflictException(String message, Throwable cause) {
        this(cause, message);
    }
}
