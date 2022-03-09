package dev.soffa.foundation.errors;

public class TimeoutException extends TechnicalException {

    private static final long serialVersionUID = 1L;

    public TimeoutException(Throwable cause, String messsage, Object... args) {
        super(cause, messsage, args);
    }

    public TimeoutException(String messsage, Object... args) {
        super(messsage, args);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
