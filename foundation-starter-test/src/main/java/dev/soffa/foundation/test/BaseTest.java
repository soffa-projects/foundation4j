package dev.soffa.foundation.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseTest {

    @Autowired
    private ApplicationContext context;

    protected String getProperty(String key) {
        return context.getEnvironment().getProperty(key);
    }

}
