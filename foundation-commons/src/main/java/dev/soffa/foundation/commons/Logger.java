package dev.soffa.foundation.commons;

import com.mgnt.utils.TextUtils;
import dev.soffa.foundation.error.ErrorUtil;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("PMD.MoreThanOneLogger")
public final class Logger {

    public static Logger app = Logger.get("dev.soffa");
    public static Logger platform = Logger.get("dev.soffa");

    static {
        Logger.setRelevantPackage("dev.soffa");
    }

    private final org.slf4j.Logger log;
    private String tag;

    private Logger(org.slf4j.Logger logger) {
        this.log = logger;
    }

    private Logger(org.slf4j.Logger logger, String tag) {
        this(logger);
        this.tag = tag;
    }

    public static void withContext(Map<String, String> context, Consumer<Logger> consumer) {
        withContext(context, logger -> {
            consumer.accept(app);
            return null;
        });
    }

    public static void withContext(Map<String, String> context, Runnable runnable) {
        withContext(context, logger -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T withContext(Map<String, String> context, Supplier<T> supplier) {
        return withContext(context, logger -> {
            return supplier.get();
        });
    }

    public static <T> T withContext(Map<String, String> context, Function<Logger, T> fn) {
        Map<String, String> current = org.slf4j.MDC.getCopyOfContextMap();
        if (current == null) {
            current = new HashMap<>();
        }
        Map<String, String> backup = new HashMap<>(current);
        try {
            current.putAll(context);
            setContext(current);
            return fn.apply(app);
        } finally {
            setContext(backup);
        }
    }

    public static void setContext(Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            org.slf4j.MDC.clear();
        } else {
            context.values().removeAll(Collections.singleton(null));
            org.slf4j.MDC.setContextMap(context);
        }
    }

    public static void setTenantId(String tenantId) {
        if (TextUtil.isNotEmpty(tenantId)) {
            org.slf4j.MDC.put("tenant", tenantId);
        } else {
            org.slf4j.MDC.remove("tenant");
        }
    }

    public static void setRelevantPackage(String pkg) {
        if ("*".equals(pkg)) {
            TextUtils.setRelevantPackage(null);
        } else {
            TextUtils.setRelevantPackage(pkg);
            app = get(pkg);
        }
    }

    public static Logger getLogger(Class<?> type) {
        return get(type);
    }

    public static Logger get(Class<?> type) {
        return new Logger(LoggerFactory.getLogger(type));
    }

    public static Logger get(String name) {
        return new Logger(LoggerFactory.getLogger(name));
    }

    public static Logger get(String name, String tag) {
        return new Logger(LoggerFactory.getLogger(name), tag);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public void debug(String message, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(formatMessage(message, args));
        }
    }

    public void trace(String message, Object... args) {
        if (log.isTraceEnabled()) {
            log.trace(formatMessage(message, args));
        }
    }

    public void info(String message, Object... args) {
        if (log.isInfoEnabled()) {
            log.info(formatMessage(message, args));
        }
    }

    public void info(Map<String, String> context, String message, Object... args) {
        if (log.isInfoEnabled()) {
            Logger.withContext(context, () -> info(message, args));
        }
    }

    private String formatMessage(String message, Object... args) {
        if (TextUtil.isEmpty(tag)) {
            return TextUtil.format(message, args);
        }
        if (message.contains("{}") && !message.contains("%")) {
            return "[" + tag + "] " + MessageFormat.format(message, args);
        }
        return "[" + tag + "] " + TextUtil.format(message, args);
    }

    public void warn(String message, Object... args) {
        log.warn(formatMessage(message, args));
    }

    public void error(Throwable e) {
        error(ErrorUtil.loookupOriginalMessage(e), e);
    }

    public void error(Throwable error, String message, Object... args) {
        error(formatMessage(message, args), error);
    }

    public void error(String message, Throwable e) {
        log.error(message);
        log.error(ErrorUtil.getStacktrace(e));
    }


    public void error(String message, Object... args) {
        log.error(formatMessage(message, args));
    }

    public void error(Map<String, String> context, String message, Object... args) {
        if (log.isErrorEnabled()) {
            Logger.withContext(context, () -> error(message, args));
        }
    }

}
