package dev.soffa.foundation.starter.test;


import dev.soffa.foundation.config.ApplicationSettingTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class ApplicationSettingTemplateTest {

    @Autowired
    private ApplicationSettingTemplate settings;


    @Test
    void testSettings(){
        assertNotNull(settings);
        Optional<Map<String,Object>> result = settings.get("foo");
        assertFalse(result.isPresent());
    }
}
