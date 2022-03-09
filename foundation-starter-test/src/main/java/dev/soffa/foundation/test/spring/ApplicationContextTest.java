package dev.soffa.foundation.test.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "SpringJavaAutowiredMembersInspection"})
public abstract class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testContext() {
        assertNotNull(context);
    }

}
