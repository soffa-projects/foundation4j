package dev.soffa.foundation.test;

import com.google.gson.Gson;
import com.intuit.karate.junit5.Karate;
import org.checkerframework.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public final class KarateTestHelper {

    private static final Gson GSON = new Gson();

    public static Karate create(String... paths) {
        return create("test", null, paths);
    }

    public static Karate create(Map<String, Object> config, String... paths) {
        return create("test", config, paths);
    }

    public static Karate create(String env, Map<String, Object> config, String... paths) {
        return Karate.run(feature(paths))
            .systemProperty("config", GSON.toJson(Optional.ofNullable(config).orElse(ImmutableMap.of())))
            .karateEnv(env);
            //.clientFactory(new MockSpringMvcServlet(mvc));
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
