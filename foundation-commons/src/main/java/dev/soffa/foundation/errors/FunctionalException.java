package dev.soffa.foundation.errors;

import dev.soffa.foundation.commons.TextUtil;

public class FunctionalException extends RuntimeException implements ManagedException {

    private static final long serialVersionUID = 1L;

    public FunctionalException(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public FunctionalException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }

    public FunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
