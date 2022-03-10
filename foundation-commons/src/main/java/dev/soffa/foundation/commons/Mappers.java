package dev.soffa.foundation.commons;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class Mappers {

    public static final Mapper JSON_FULLACCESS_SNAKE = new JacksonMapper(
        ObjectMapperFactory.newJsonMapper(true, new PropertyNamingStrategies.SnakeCaseStrategy())
    );

    public static final Mapper JSON_FULLACCESS = new JacksonMapper(
        ObjectMapperFactory.newJsonMapper(true)
    );

    public static final Mapper JSON = new JacksonMapper(
        ObjectMapperFactory.newJsonMapper()
    );

    public static final Mapper YAML = new JacksonMapper(
        new YAMLMapper()
    );

    private Mappers() {
    }

    public static boolean isJson(String input) {
        try {
            new JSONObject(input);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

}
