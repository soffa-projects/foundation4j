package dev.soffa.foundation.commons;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.error.ManagedException;
import dev.soffa.foundation.error.TechnicalException;

import java.util.function.Supplier;

public interface SentryProvider {

    void captureException(Throwable e);

    default void watch(String label, Runnable runnable) {
       watch(label, runnable, true);
    }

    default void watch(String label, Runnable runnable, boolean errorPropagation) {
        watch(label, (Supplier<Void>) () -> {
            runnable.run();
            return null;
        }, errorPropagation);
    }

    default <T> T watch(String label, Supplier<T> runnable) {
        return watch(label, runnable, true);
    }

    default <T> T watch(String label, Supplier<T> runnable, boolean errorPropagation) {
        try {
            return runnable.get();
        }catch (Exception e) {
            Logger.app().error(e, "%s has failed with error: %s", label, e.getMessage());
            captureTechnical(e);
            if (errorPropagation) {
                throw e;
            }
            return null;
        }
    }

    default void captureTechnical(Throwable e) {
        if (e instanceof ManagedException) {
            if (e instanceof TechnicalException) {
                captureException(e);
            }
        } else {
            captureException(e);
        }
    }

    default void captureEvent(Context context, String message, EventLevel level) {
        captureEvent(context, null, message, level);
    }

    void captureEvent(Context context, String messageId, String message, EventLevel level);

    default void captureEvent(Context context, String messageId, String message) {
        captureEvent(context, messageId, message, EventLevel.INFO);
    }

    default void captureEvent(Context context, String event, String messageId, Runnable runnable) {
        try {
            runnable.run();
            captureEvent(context, messageId, event, EventLevel.INFO);
        } catch (Exception ex) {
            captureException(ex);
            throw ex;
        }
    }

    default void captureEvent(Context context, String event, Runnable runnable) {
        captureEvent(context, event, null, runnable);
    }

    default void captureError(Context context, String message) {
        captureEvent(context, message, EventLevel.ERROR);
    }

    default void captureEvent(Context context, String message) {
        captureEvent(context, message, EventLevel.INFO);
    }

    class DefaultAdapter implements SentryProvider {

        @Override
        public void captureException(Throwable e) {
            // Nothing
        }

        @Override
        public void captureEvent(Context context, String messageId, String message, EventLevel level) {
            // Do nothing
        }
    }
}
