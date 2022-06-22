package dev.soffa.foundation.commons;

import dev.soffa.foundation.context.Context;

public interface SentryProvider {

    void captureException(Throwable e);

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
        }catch (Exception ex) {
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
