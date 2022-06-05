package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.config.ConfigManager;
import dev.soffa.foundation.starter.test.app.model.ConfigModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    properties = {
        "foo.bar.enabled=true",
        "foo.bar.interval=every 10s"
    }
)
@ActiveProfiles("test")
public class ConfigManagerTest {

    @Autowired
    private ConfigManager configManager;

    @Test
    public void testScheduler() {
        assertNotNull(configManager);
        ConfigModel model = configManager.bind("foo.bar", ConfigModel.class);
        assertNotNull(model);
        assertTrue(model.isEnabled());
        assertEquals("every 10s", model.getInterval());
    }

}
