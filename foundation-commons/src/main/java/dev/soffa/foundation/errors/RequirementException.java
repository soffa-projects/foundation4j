package dev.soffa.foundation.errors;

public class RequirementException extends FunctionalException {

    private static final long serialVersionUID = 1L;

    public RequirementException(String message, Object... args) {
        super(message, args);
    }

    public RequirementException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public RequirementException(String message, Throwable cause) {
        super(message, cause);
    }

}
