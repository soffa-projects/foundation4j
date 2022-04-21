package dev.soffa.foundation.commons;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.UUID;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String generate() {
        return generate("");
    }

    public static String generate(String... prefix) {
        return TextUtil.prefix(UUID.randomUUID().toString(), prefix);
    }
    public static String secureRandomId() {
        return secureRandomId("");
    }

    public static String secureRandomId(String prefix) {
        return TextUtil.prefix(NanoIdUtils.randomNanoId(), prefix);
    }


}
