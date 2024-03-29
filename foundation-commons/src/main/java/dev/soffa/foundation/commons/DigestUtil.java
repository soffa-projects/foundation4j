package dev.soffa.foundation.commons;

import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;
import java.util.UUID;

public final class DigestUtil {

    private DigestUtil() {
    }

    public static UUID makeUUID(String raw) {
        if (TextUtil.isEmpty(raw)) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            String uuid0 = DigestUtil.md5(raw);
            uuid0 = uuid0.substring(0, 8) + "-" + uuid0.substring(8, 12) + "-" + uuid0.substring(12, 16) + "-" + uuid0.substring(16, 20) + "-" + uuid0.substring(20);
            return UUID.fromString(uuid0);
        }
    }

    public static String md5(String value) {
        if (TextUtil.isEmpty(value)) {
            return "";
        }
        return DigestUtils.md5Hex(value);
    }

    @SneakyThrows
    public static String md5(InputStream io) {
        if (io==null) {
            return "";
        }
        return DigestUtils.md5Hex(io);
    }

}
