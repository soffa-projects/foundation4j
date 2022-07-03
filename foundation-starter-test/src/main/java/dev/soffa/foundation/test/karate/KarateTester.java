package dev.soffa.foundation.test.karate;

import com.intuit.karate.http.HttpClientFactory;
import com.intuit.karate.junit5.Karate;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.test.KarateTestHelper;
import lombok.AllArgsConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;

@AllArgsConstructor
public final class KarateTester {

    private final HttpClientFactory clientFactory;

    public static KarateTester of(MockMvc mokcMvc) {
        return new KarateTester(new MockMvcHttpClient(mokcMvc));
    }

    private String[] feature(String... paths) {
        return Arrays.stream(paths).map(KarateTestHelper::feature).toArray(String[]::new);
    }

    private String feature(String path) {
        if (path.endsWith(".feature")) {
            return path;
        }
        return "classpath:feature/" + path + ".feature";
    }

    public Karate create(String... paths) {
        return create("test", null, paths);
    }

    public Karate create(Map<String, Object> config, String... paths) {
        return create("test", config, paths);
    }

    public Karate create(String env, Map<String, Object> config, String... paths) {
        return Karate.run(feature(paths))
            .systemProperty("config", Mappers.JSON_DEFAULT.serialize(config))
            .karateEnv(env)
            .clientFactory(this.clientFactory);
    }

}
