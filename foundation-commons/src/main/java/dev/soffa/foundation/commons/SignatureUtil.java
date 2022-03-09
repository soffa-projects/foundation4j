package dev.soffa.foundation.commons;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

public final class SignatureUtil {

    private SignatureUtil() {
    }

    public static String sign(String input, String secret) {
        HmacUtils hm256 = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret);
        return hm256.hmacHex(input);
    }

}
