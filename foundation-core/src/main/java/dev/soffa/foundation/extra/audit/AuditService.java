package dev.soffa.foundation.extra.audit;

import dev.soffa.foundation.context.Context;

public interface AuditService {

    default void log(String action, Object input, Object output, Context context) {
        // Nothing to see here
    }

    default void log(String action, Object input, Object output, String error, Context context) {
        // Nothing to see here
    }

    default void log(String action, Object input, Object output, Exception e, Context context) {
        // Nothing to see here
    }

}
