package dev.soffa.foundation.test.karate;

import com.intuit.karate.junit5.Karate;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.test.KarateTestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;

@AutoConfigureMockMvc
public class KarateMockMvcTest {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private MockMvc mvc;

    public Karate create(String... paths) {
        return create("test", null, paths);
    }

    public Karate create(Map<String, Object> config, String... paths) {
        return create("test", config, paths);
    }

    public Karate create(String env, Map<String, Object> config, String... paths) {
        return Karate.run(feature(paths))
            .systemProperty("config", Mappers.JSON.serialize(config))
            .karateEnv(env)
            .clientFactory(new MockSpringMvcServlet(mvc));
    }

    private static String[] feature(String... paths) {
        return Arrays.stream(paths).map(KarateTestHelper::feature).toArray(String[]::new);
    }

    public static String feature(String path) {
        if (path.endsWith(".feature")) {
            return path;
        }
        return "classpath:feature/" + path + ".feature";
    }

}
