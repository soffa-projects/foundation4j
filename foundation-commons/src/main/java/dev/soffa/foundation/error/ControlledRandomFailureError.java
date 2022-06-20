package dev.soffa.foundation.error;

import dev.soffa.foundation.commons.TextUtil;

public class ControlledRandomFailureError extends RuntimeException implements ManagedException {

    private static final long serialVersionUID = 1L;

    public ControlledRandomFailureError(String message, Object... args) {
        super(TextUtil.format(message, args));
    }

    public ControlledRandomFailureError(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }

    public ControlledRandomFailureError(String message, Throwable cause) {
        super(message, cause);
    }

    public ControlledRandomFailureError(Throwable cause) {
        super(cause);
    }

}
