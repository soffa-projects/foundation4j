package dev.soffa.foundation.errors;

public class TodoException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public TodoException() {
        this("Not implementation yet");
    }

    public TodoException(String messsage, Object... args) {
        super(messsage, args);
    }

    public TodoException(String message, Throwable cause) {
        super(message, cause);
    }

}
