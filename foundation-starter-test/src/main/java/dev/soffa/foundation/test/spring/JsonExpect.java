package dev.soffa.foundation.test.spring;

import lombok.SneakyThrows;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class JsonExpect {

    private final ResultActions result;

    JsonExpect(ResultActions result) {
        this.result = result;
    }

    @SneakyThrows
    public JsonExpect eq(String path, Object value) {
        result.andExpect(jsonPath(path).value(Matchers.equalTo(value)));
        return this;
    }

    @SneakyThrows
    public JsonExpect isArray(String path) {
        result.andExpect(jsonPath(path).isArray());
        return this;
    }

    /**
     * Check if object has key <code>path</code>
     *
     * @param path key to check
     */
    @SneakyThrows
    public JsonExpect exists(String path) {
        result.andExpect(jsonPath(path).exists());
        return this;
    }

    @SneakyThrows
    public JsonExpect doesNotExist(String path) {
        result.andExpect(jsonPath(path).doesNotExist());
        return this;
    }

    @SneakyThrows
    public JsonExpect value(String path, Matcher<? super Object> matcher) {
        result.andExpect(jsonPath(path).value(matcher));
        return this;
    }

    @SneakyThrows
    public JsonExpect matches(String path, String regex) {
        result.andExpect(jsonPath(path).value(Matchers.matchesRegex(regex)));
        return this;
    }


}
