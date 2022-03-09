package dev.soffa.foundation.context;

import dev.soffa.foundation.commons.TextUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RequestContextUtil {

    private RequestContextUtil() {
    }

    public static Map<String, Object> tagify(Context context) {
        return tagify(context, null);
    }

    public static Map<String, Object> tagify(Context context, Map<String, Object> more) {
        String sessionId = context.getAuthorization();
        if (TextUtil.isNotEmpty(sessionId)) {
            sessionId = DigestUtils.md5Hex(sessionId);
        }
        Map<String, Object> tags = createTags(
            "ctx_access", context.isAuthenticated() ? "authenticated" : "anonymous",
            "ctx_tenant", context.getTenantId(),
            "ctx_application", context.getApplicationName(),
            "ctx_source", context.getSender(),
            "ctx_username", context.getUsername(),
            "ctx_session_id", sessionId
        );
        if (more != null) {
            tags.putAll(more);
        }
        return tags;
    }

    @SuppressWarnings("DuplicatedCode")
    private static Map<String, Object> createTags(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("MapUtil.create() requires an even number of arguments");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("MapUtil.create() requires String keys");
            }
            result.put(args[i].toString(), args[i + 1]);
        }
        return result;
    }


}
