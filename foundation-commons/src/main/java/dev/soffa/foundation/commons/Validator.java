package dev.soffa.foundation.commons;

import dev.soffa.foundation.commons.validation.ValidationResult;
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

    public Validator checkNotNull(String field, Object value) {
        return checkNotNull(field, value, "is required");
    }

    @SneakyThrows
    public Validator checkNotNull(String field, Object value, String message) {
        boolean isNullOrEmpty = value == null;
        if (!isNullOrEmpty) {
            if (value instanceof String) {
                isNullOrEmpty = ((String) value).isEmpty();
            } else if (value instanceof VO) {
                isNullOrEmpty = ((VO) value).getValue().isEmpty();
            }
        }
        return check(field, message, isNullOrEmpty);
    }

    public Validator check(String field, String message, Supplier<Boolean> tester) {
        return check(field, message, tester.get());
    }

    /**
     * @param field   Field under validation
     * @param message The message to display when the condition vailes
     * @param test    true means the condition will fail
     * @return validator instance for chaining
     */
    public Validator check(String field, String message, Boolean test) {
        if (test) {
            errors.put(field, message);
        }
        return this;
    }

    @Override
    public Map<String, String> getErrors() {
        return errors;
    }

    @Override
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


    @SneakyThrows
    @Override
    public void thowAnyError() {
        if (hasErrors()) {
            throw new ValidationException(errors);
        }
    }

    @Override
    public void printErrors(String message) {
        if (hasErrors()) {
            Logger.app.error("%s -- %s", message, Mappers.JSON_DEFAULT.serialize(errors));
        }
    }
}
