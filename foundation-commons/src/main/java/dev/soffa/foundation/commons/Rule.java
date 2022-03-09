package dev.soffa.foundation.commons;

import lombok.SneakyThrows;
import net.fortuna.ical4j.model.ValidationException;

import java.util.function.Supplier;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class Rule {

    private Rule() {
    }

    @SneakyThrows
    public static void check(String description, Supplier<Boolean> supplier) {
        check(description, supplier.get());
    }

    @SneakyThrows
    public static void check(String description, Boolean checked) {
        if (!checked) {
            throw new ValidationException(description);
        }
    }

}
