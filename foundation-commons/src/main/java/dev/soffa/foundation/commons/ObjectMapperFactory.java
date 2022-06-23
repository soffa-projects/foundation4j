package dev.soffa.foundation.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Date;

public final class ObjectMapperFactory {

    private ObjectMapperFactory() {
    }

    public static ObjectMapper newJsonMapper() {
        return newJsonMapper(false, null);
    }

    public static ObjectMapper newJsonMapper(boolean ignoreAccessAnnotations) {
        return newJsonMapper(ignoreAccessAnnotations, null);
    }

    public static ObjectMapper newJsonMapper(boolean ignoreAccessAnnotations, PropertyNamingStrategy strategy) {
        ObjectMapper mapper = JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .build();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer() {
            @Override
            public Date deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                try {
                    return super.deserialize(parser, ctxt);
                } catch (InvalidFormatException e) {
                    if (parser.hasToken(JsonToken.VALUE_STRING)) {
                        String string = parser.getText().trim();
                        return DateUtil.parse(string);
                    }
                    throw e;
                }
            }
        });

        if (ignoreAccessAnnotations) {
            mapper.setAnnotationIntrospector(new IgnoreAnnotations());
        }

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        if (strategy != null) {
            mapper.setPropertyNamingStrategy(strategy);
        }

        return mapper
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
            .registerModule(simpleModule);
    }

    public static class IgnoreAnnotations extends JacksonAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public JsonProperty.Access findPropertyAccess(Annotated m) {
            return null;
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            return false;
        }
    }
}
