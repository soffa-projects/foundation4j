package dev.soffa.foundation.application;

import dev.soffa.foundation.commons.DefaultIdGenerator;
import dev.soffa.foundation.commons.IdGenerator;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class ID {

    public static IdGenerator generator = new DefaultIdGenerator();

    public static String generate() {
        return generator.nextId("");
    }

    public static String generate(String prefix) {
        return generator.nextId(prefix);
    }
}
