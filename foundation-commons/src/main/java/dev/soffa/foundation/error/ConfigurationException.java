package dev.soffa.foundation.error;

import dev.soffa.foundation.commons.TextUtil;

public class ConfigurationException extends RuntimeException implements ManagedException {

    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message, Object... args) {
        this(null, message, args);
    }

    public ConfigurationException(Throwable cause, String message, Object... args) {
        super(TextUtil.format(message, args), cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        this(cause, message);
    }

}
