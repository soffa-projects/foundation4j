package dev.soffa.foundation.tracing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "spring.application.name=tracing-app",
    "ZIPKIN_ENABLED=false"
})
@ActiveProfiles({"test", "foundation"})
public class TestTracingDisabled {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testContext() {
        assertNotNull(context);
    }

}
