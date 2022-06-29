package dev.soffa.foundation.error;

public class NotImplementedException extends TechnicalException {

    private static final long serialVersionUID = 1L;

    public NotImplementedException() {
        super("Not implemented");
    }

    public NotImplementedException(Throwable cause, String messsage, Object... args) {
        super(cause, messsage, args);
    }

    public NotImplementedException(String messsage, Object... args) {
        super(messsage, args);
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

}
