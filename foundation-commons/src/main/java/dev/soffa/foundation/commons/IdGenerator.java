package dev.soffa.foundation.commons;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

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

    public static String uuid() {
        return generate("");
    }

    public static String uuid(String... prefix) {
        return TextUtil.prefix(UUID.randomUUID().toString(), prefix);
    }

    public static String nanoId() {
        return nanoId("");
    }

    public static String nanoId(String... prefix) {
        return TextUtil.prefix(NanoIdUtils.randomNanoId(), prefix);
    }

    /**
     * Not garuantee of uniqueness
     @return Generated short id
     */
    public static String shortId() {
        return shortId(false);
    }

    /**
     * Not garuantee of uniqueness
     * @param compact lowercase with no dashes or underscores
     * @return Generated short id
     */
    public static String shortId(boolean compact) {
        return shortId(compact, "");
    }

    /**
     * Not garuantee of uniqueness
     * @param compact Lowercase id with no dashes or underscores
     * @param prefix Prefix to add to the id
     * @return Generated short id
     */
    public static String shortId(boolean compact, String... prefix) {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());

        String res = Base64.getEncoder().withoutPadding().encodeToString(byteBuffer.array())
            .replaceAll("/", "_")
            .replaceAll("\\+", "-");

        if (compact) {
            res = res.toLowerCase().replaceAll("-", "").replaceAll("_", "");
        }

        return TextUtil.prefix(res, prefix);
    }


}
