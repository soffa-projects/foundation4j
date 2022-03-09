package dev.soffa.foundation.errors;

import dev.soffa.foundation.commons.TextUtil;

public class TechnicalException extends RuntimeException implements ManagedException {

    private static final long serialVersionUID = 1L;

    public TechnicalException(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public TechnicalException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public TechnicalException(Throwable cause) {
        super(cause);
    }

}
