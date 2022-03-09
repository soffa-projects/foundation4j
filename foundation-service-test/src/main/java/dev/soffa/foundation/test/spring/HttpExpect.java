package dev.soffa.foundation.test.spring;

import lombok.AllArgsConstructor;
import org.springframework.test.web.servlet.MockMvc;

@AllArgsConstructor
public class HttpExpect {

    private final MockMvc mvc;

    public HttpRequest get(String uri) {
        return new HttpRequest(mvc, "GET", uri);
    }

    public HttpRequest post(String uri) {
        return new HttpRequest(mvc, "POST", uri);
    }

    public HttpRequest delete(String uri) {
        return new HttpRequest(mvc, "DELETE", uri);
    }

    public HttpRequest put(String uri) {
        return new HttpRequest(mvc, "PUT", uri);
    }

    public HttpRequest patch(String uri) {
        return new HttpRequest(mvc, "PATCH", uri);
    }

    public HttpRequest head(String uri) {
        return new HttpRequest(mvc, "HEAD", uri);
    }

    public HttpRequest options(String uri) {
        return new HttpRequest(mvc, "OPTIONS", uri);
    }


}
