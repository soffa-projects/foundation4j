package dev.soffa.foundation.commons.http;

import dev.soffa.foundation.commons.TextUtil;
import lombok.Data;

import javax.ws.rs.HttpMethod;
import java.util.HashMap;
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

    public static HttpRequest get(String url) {
        return new HttpRequest(HttpMethod.GET, url);
    }

    public static HttpRequest post(String url) {
        return new HttpRequest(HttpMethod.POST, url);
    }

    public static HttpRequest patch(String url) {
        return new HttpRequest(HttpMethod.PATCH, url);
    }

    public static HttpRequest put(String url) {
        return new HttpRequest(HttpMethod.PUT, url);
    }

    public static HttpRequest delete(String url) {
        return new HttpRequest(HttpMethod.DELETE, url);
    }

    public HttpRequest bearer(String token) {
        return authorization("Bearer " + token);
    }

    public HttpRequest authorization(String value) {
        return header("Authorization", value);
    }

    public HttpRequest withBody(Object body) {
        this.body = body;
        return this;
    }

    public HttpRequest withContentType(String type) {
        this.contentType = type;
        return this;
    }

    public HttpRequest header(String name, String value) {
        if (TextUtil.isNotEmpty(value)) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(name, value);
        }
        return this;
    }


}
