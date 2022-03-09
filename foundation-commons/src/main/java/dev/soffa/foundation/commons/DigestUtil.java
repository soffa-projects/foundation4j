package dev.soffa.foundation.commons;

import org.apache.commons.codec.digest.DigestUtils;

public final class DigestUtil {

    private DigestUtil() {
    }

    public static String md5(String value) {
        if (TextUtil.isEmpty(value)) {
            return null;
        }
        return DigestUtils.md5Hex(value);
    }

}
