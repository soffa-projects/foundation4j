package dev.soffa.foundation.errors;

public class InvalidTenantException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public InvalidTenantException(String message, Object... args) {
        super(message, args);
    }

    public InvalidTenantException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public InvalidTenantException(String message, Throwable cause) {
        super(message, cause);
    }

}
