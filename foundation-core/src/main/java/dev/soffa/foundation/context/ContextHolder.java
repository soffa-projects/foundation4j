package dev.soffa.foundation.context;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.http.HttpContextHolder;
import dev.soffa.foundation.errors.FunctionalException;

import java.util.Optional;

public final class ContextHolder {

    private static final ThreadLocal<Context> CURRENT = new InheritableThreadLocal<>();

    private ContextHolder() {
    }

    public static void set(Context value) {
        if (value == null) {
            CURRENT.remove();
            HttpContextHolder.clear();
            Logger.setContext(null);
        } else {
            CURRENT.set(value);
            Logger.setContext(value.getContextMap());
            HttpContextHolder.set(value.getHeaders());
        }
    }

    public static void clear() {
        set(null);
    }

    public static boolean isEmpty() {
        return CURRENT.get() == null;
    }

    public static Optional<Context> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Context inheritOrCreate() {
        return Optional.ofNullable(CURRENT.get()).orElse(new Context());
    }

    public static Context require() {
        return Optional.ofNullable(CURRENT.get()).orElseThrow(() -> new FunctionalException("MISSING_CONTEXT"));
    }

}
