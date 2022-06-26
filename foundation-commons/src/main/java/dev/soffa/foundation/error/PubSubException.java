package dev.soffa.foundation.error;

public class PubSubException extends TechnicalException {

    private static final long serialVersionUID = 1L;

    public PubSubException(Throwable cause, String messsage, Object... args) {
        super(cause, messsage, args);
    }

    public PubSubException(String messsage, Object... args) {
        super(messsage, args);
    }

    public PubSubException(String message, Throwable cause) {
        super(message, cause);
    }

}
