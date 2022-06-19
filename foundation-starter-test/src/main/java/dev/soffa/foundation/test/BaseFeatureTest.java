package dev.soffa.foundation.test;

import com.intuit.karate.junit5.Karate;
import dev.soffa.foundation.test.karate.KarateTester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Karate.Test
    Karate testFeature() {
        return KarateTester.of(mockMvc).create(getTestData(), getFeatures());
    }

    public abstract String[] getFeatures();

    public abstract Map<String, Object> getTestData();


}
