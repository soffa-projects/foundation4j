package dev.soffa.foundation.commons;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String shortUUID() {
        return shortUUID("");
    }

    public static String shortUUID(String... prefix) {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return TextUtil.prefix(Long.toString(l, Character.MAX_RADIX), prefix);
    }

    public static String secureRandomId() {
        return secureRandomId("");
    }

    public static String secureRandomId(String prefix) {
        return TextUtil.prefix(NanoIdUtils.randomNanoId(), prefix);
    }


}
