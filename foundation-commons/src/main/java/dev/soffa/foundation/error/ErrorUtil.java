package dev.soffa.foundation.error;

import com.mgnt.utils.TextUtils;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.model.ResponseStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ErrorUtil {

    private static final String ALL_PACKAGES = "*";
    private static final Map<Class<?>, Integer> MAPPED_STATUS = new LinkedHashMap<>();
    private static String defaultErrorPackage = "*";

    static {
        MAPPED_STATUS.put(DatabaseException.class, ResponseStatus.SERVER_ERROR);
        MAPPED_STATUS.put(ConfigurationException.class, ResponseStatus.SERVER_ERROR);
        MAPPED_STATUS.put(RequirementException.class, ResponseStatus.EXPECTATION_FAILED);
        MAPPED_STATUS.put(NotImplementedException.class, ResponseStatus.NOT_IMLEMENTED);
        MAPPED_STATUS.put(InvalidTenantException.class, ResponseStatus.BAD_REQUEST);
        MAPPED_STATUS.put(ValidationException.class, ResponseStatus.BAD_REQUEST);
        MAPPED_STATUS.put(ConflictException.class, ResponseStatus.CONFLICT);
        MAPPED_STATUS.put(ForbiddenException.class, ResponseStatus.FORBIDDEN);
        MAPPED_STATUS.put(UnauthorizedException.class, ResponseStatus.UNAUTHORIZED);
        MAPPED_STATUS.put(InvalidTokenException.class, ResponseStatus.UNAUTHORIZED);
        MAPPED_STATUS.put(InvalidAuthException.class, ResponseStatus.UNAUTHORIZED);
        MAPPED_STATUS.put(ResourceNotFoundException.class, ResponseStatus.NOT_FOUND);
        MAPPED_STATUS.put(NoContentException.class, ResponseStatus.NO_CONTENT);
        MAPPED_STATUS.put(TodoException.class, ResponseStatus.NOT_IMLEMENTED);
        MAPPED_STATUS.put(SocketException.class, ResponseStatus.REQUEST_TIMEOUT);
        MAPPED_STATUS.put(TimeoutException.class, ResponseStatus.REQUEST_TIMEOUT);
        MAPPED_STATUS.put(TechnicalException.class, ResponseStatus.SERVER_ERROR);
        MAPPED_STATUS.put(FunctionalException.class, ResponseStatus.BAD_REQUEST);
    }

    private ErrorUtil() {
    }

    public static void setRelevantPackage(String pkg) {
        defaultErrorPackage = pkg;
        if (!ALL_PACKAGES.equals(pkg)) {
            TextUtils.setRelevantPackage(pkg);
        }
    }

    public static String getStacktrace(Throwable e) {
        if ("*".equals(defaultErrorPackage)) {
            return TextUtils.getStacktrace(e, true);
        }
        return TextUtils.getStacktrace(e, true, defaultErrorPackage);
    }

    public static Throwable unwrap(Throwable error) {
        if (error instanceof InvocationTargetException && error.getCause() != null) {
            return unwrap(error.getCause());
        }
        if (error instanceof UndeclaredThrowableException && error.getCause() != null) {
            return unwrap(error.getCause());
        }
        if (error instanceof RuntimeException && error.getCause() != null) {
            return unwrap(error.getCause());
        }
        return error;
    }

    public static String getError(Throwable error) {
        return loookupOriginalMessage(error, null);
    }

    /**
     * See <code>getError</>
     */
    @Deprecated
    public static String loookupOriginalMessage(Throwable error) {
        return loookupOriginalMessage(error, null);
    }

    public static String loookupOriginalMessage(Throwable error, Class<?> lookup) {
        if (error == null) {
            return "Unknown error";
        }
        if (lookup != null && lookup.isInstance(error)) {
            if (TextUtil.isEmpty(error.getMessage())) {
                return loookupOriginalMessage(error.getCause(), lookup);
            }
            return error.getMessage();
        }
        if (error.getCause() != null) {
            return loookupOriginalMessage(error.getCause(), lookup);
        }
        if (TextUtil.isEmpty(error.getMessage())) {
            return loookupOriginalMessage(error.getCause(), lookup);
        }
        return error.getMessage();
    }

    public static Exception getException(int errorCode, String message) {
        switch (errorCode) {
            case ResponseStatus.BAD_REQUEST:
                return new FunctionalException(message);
            case ResponseStatus.CONFLICT:
                return new ConflictException(message);
            case ResponseStatus.FORBIDDEN:
                return new ForbiddenException(message);
            case ResponseStatus.UNAUTHORIZED:
                return new UnauthorizedException(message);
            case ResponseStatus.REQUEST_TIMEOUT:
                return new TimeoutException(message);
            default:
                return new TechnicalException(message);
        }
    }

    public static int resolveErrorCode(Throwable e) {
        if (e == null) {
            return -1;
        }
        for (Map.Entry<Class<?>, Integer> entry : MAPPED_STATUS.entrySet()) {
            if (entry.getKey().isAssignableFrom(e.getClass())) {
                return entry.getValue();
            }
        }
        return -1;
    }
}
