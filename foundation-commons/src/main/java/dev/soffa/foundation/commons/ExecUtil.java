package dev.soffa.foundation.commons;

public final class ExecUtil {

    private static final Logger LOG = Logger.get(ExecUtil.class);

    private ExecUtil() {
    }

    public static void safe(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            LOG.error("[safe-exec] failed to persist syslog", e);
        }
    }
}
