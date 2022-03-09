package dev.soffa.foundation.models;

import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.jupiter.api.Test;

public class ModelsTest {

    @Test
    public void testPojo() {
        Validator validator = ValidatorBuilder.create()
            .with(new SetterTester())
            .with(new GetterTester())
            .build();
        validator.validate("dev.soffa.foundation.models", new FilterPackageInfo());
    }
}
