package dev.soffa.foundation.commons;

import java.security.SecureRandom;
import java.util.Base64;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class IdGenerator {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {
    }

    public static String generate() {
        return generate("");
    }

    public static String generate(String... prefix) {
        byte[] buffer = new byte[20];
        RANDOM.nextBytes(buffer);
        return TextUtil.prefix(ENCODER.encodeToString(buffer), prefix);
    }

}
