package dev.soffa.foundation.test.spring;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.jsonpath.JsonPath;
import dev.soffa.foundation.commons.Mappers;
import lombok.SneakyThrows;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.function.Consumer;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class HttpResult {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    static {
        SimpleModule simpleModule = new SimpleModule();
        MAPPER.registerModule(simpleModule);
    }

    private final ResultActions result;

    HttpResult(ResultActions result) {
        this.result = result;
    }

    @SneakyThrows
    public HttpResult isOK() {
        result.andExpect(MockMvcResultMatchers.status().isOk());
        return this;
    }


    @SneakyThrows
    public HttpResult print() {
        result.andDo(MockMvcResultHandlers.print());
        return this;
    }

    @SneakyThrows
    public HttpResult isCreated() {
        result.andExpect(MockMvcResultMatchers.status().isCreated());
        return this;
    }

    @SneakyThrows
    public HttpResult isBadRequest() {
        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
        return this;
    }


    @SneakyThrows
    public HttpResult isNotImplemented() {
        result.andExpect(MockMvcResultMatchers.status().isNotImplemented());
        return this;
    }

    @SneakyThrows
    public HttpResult status(int statusCode) {
        result.andExpect(MockMvcResultMatchers.status().is(statusCode));
        return this;
    }

    @SneakyThrows
    public HttpResult is2xxSuccessful() {
        result.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        return this;
    }

    @SneakyThrows
    public HttpResult is3xxRedirection() {
        result.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
        return this;
    }

    @SneakyThrows
    public HttpResult is4xxClientError() {
        result.andExpect(MockMvcResultMatchers.status().is4xxClientError());
        return this;
    }

    @SneakyThrows
    public HttpResult is5xxServerError() {
        result.andExpect(MockMvcResultMatchers.status().is5xxServerError());
        return this;
    }

    @SneakyThrows
    public HttpResult isForbidden() {
        result.andExpect(MockMvcResultMatchers.status().isForbidden());
        return this;
    }

    @SneakyThrows
    public HttpResult isUnauthorized() {
        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
        return this;
    }

    @SneakyThrows
    public HttpResult json(String path, Matcher<?> matcher) {
        result.andExpect(jsonPath(path).isNotEmpty()).andExpect(jsonPath(path).value(matcher));
        return this;
    }

    @SneakyThrows
    public HttpResult json(String path, String value) {
        result.andExpect(jsonPath(path).isNotEmpty()).andExpect(jsonPath(path).value(Matchers.equalTo(value)));
        return this;
    }

    @SneakyThrows
    public HttpResult json(Consumer<JsonExpect> tester) {
        tester.accept(new JsonExpect(result));
        return this;
    }

    @SneakyThrows
    public <T> T read(Class<T> expectedClass) {
        return MAPPER.readValue(result.andReturn().getResponse().getContentAsString(), expectedClass);
    }

    @SneakyThrows
    public HttpResult hasJson(String path) {
        result.andExpect(jsonPath(path).exists());
        return this;
    }

    @SneakyThrows
    public <T> T readJson(String path, Class<T> type) {
        result.andExpect(jsonPath(path).exists());
        Object value = JsonPath.read(string(), path);
        return Mappers.JSON_DEFAULT.convert(value, type);
    }

    @SneakyThrows
    public String readJson(String path) {
        return readJson(path, String.class);
    }

    @SneakyThrows
    public HttpResult contentIs(String content) {
        result.andExpect(MockMvcResultMatchers.content().string(content));
        return this;
    }

    @SneakyThrows
    public String text() {
        return this.string();
    }

    @SneakyThrows
    public String string() {
        return result.andReturn().getResponse().getContentAsString();
    }
}
