package dev.soffa.foundation.commons;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.github.f4b6a3.uuid.UuidCreator;

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

    public static String secret() {
        return secret("");
    }

    public static String secret(String... prefix) {
        byte[] buffer = new byte[20];
        RANDOM.nextBytes(buffer);
        return TextUtil.prefix(ENCODER.encodeToString(buffer), prefix);
    }

    public static String uuidSnakeCase(String... prefix) {
        return uuid(prefix).replaceAll("-", "_");
    }

    public static String uuid(String... prefix) {
        return TextUtil.prefix(UuidCreator.getRandomBased().toString(), prefix);
    }

    public static String nanoId() {
        return nanoId("");
    }

    public static String nanoId(String... prefix) {
        return TextUtil.prefix(NanoIdUtils.randomNanoId(), prefix);
    }

    /**
     * Not garuantee of uniqueness
     *
     * @return Generated short id
     */
    public static String shortId() {
        return shortId(false);
    }

    /**
     * Not garuantee of uniqueness
     *
     * @param compact lowercase with no dashes or underscores
     * @return Generated short id
     */
    public static String shortId(boolean compact) {
        return shortId(compact, "");
    }

    /**
     * Not garuantee of uniqueness
     *
     * @param compact Lowercase id with no dashes or underscores
     * @param prefix  Prefix to add to the id
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
