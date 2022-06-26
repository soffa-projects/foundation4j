package dev.soffa.foundation.commons.validation;

import java.util.Map;

public interface ValidationResult {

    Map<String,String> getErrors();

    boolean hasErrors();

}
