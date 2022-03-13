package dev.soffa.foundation.commons;

import dev.soffa.foundation.error.ValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public final class ValidationUtil {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

    private ValidationUtil() {
    }

    public static <T> void validate(T input) {
        Validator validator = FACTORY.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(input);
        if (violations.isEmpty()) {
            return;
        }
        ConstraintViolation<T> c = violations.iterator().next();
        String prop = c.getPropertyPath().toString();
        throw new ValidationException(prop, prop + " " + c.getMessage());
    }
}
