package dev.soffa.foundation.commons.validation;

import dev.soffa.foundation.error.ValidationException;
import dev.soffa.foundation.model.VO;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@NoArgsConstructor
public class Validator implements ValidationResult {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

    private final Map<String, String> errors = new HashMap<>();


    @SneakyThrows
    public Validator checkNotNull(String field, Object value, String message) {
        boolean isNotNull = value != null;
        if (isNotNull && value instanceof VO) {
            isNotNull = ((VO) value).getValue() != null;
        }
        return check(field, message, isNotNull);
    }

    public Validator check(String field, String message, Supplier<Boolean> tester) {
        return check(field, message, tester.get());
    }

    public Validator check(String field, String message, Boolean test) {
        if (!test) {
            errors.put(field, message);
        }
        return this;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public static <T> Validator check(T subject) {
        return new Validator().validate(subject);
    }

    public <T> Validator validate(T subject) {
        javax.validation.Validator validator = FACTORY.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(subject);
        if (violations.isEmpty()) {
            return this;
        }
        for (ConstraintViolation<?> violation : violations) {
            String prop = violation.getPropertyPath().toString();
            errors.put(prop, violation.getMessage());
        }
        return this;
    }


    public void thowAnyError() throws ValidationException {
        if (hasErrors()) {
            throw new ValidationException(errors);
        }
    }
}
