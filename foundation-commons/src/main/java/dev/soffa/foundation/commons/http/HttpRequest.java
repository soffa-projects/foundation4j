package dev.soffa.foundation.commons.http;

import lombok.Data;

import java.util.Map;

@Data
public class HttpRequest {

    private String method;
    private String url;
    private Object body;
    private Map<String, String> headers;
    private String contentType = "application/json";

    // void expect(Consumer<HttpResponseExpectation> consumer);

    public HttpRequest(String method, String url) {
        this(method, url, null);
    }

    public HttpRequest(String method, String url, Object body) {
        this(method, url, body, null);
    }

    public HttpRequest(String method, String url, Object data, Map<String, String> headers) {
        this.method = method;
        this.url = url;
        this.body = data;
        this.headers = headers;
    }

}
