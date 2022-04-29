package dev.soffa.foundation.test.spring;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.http.HttpUtil;
import dev.soffa.foundation.context.Context;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.util.Collections;

public class HttpRequest {

    private final String method;
    private final String uri;
    private final MockMvc mvc;
    private final HttpHeaders headers = new HttpHeaders();
    private MediaType contentType = MediaType.APPLICATION_JSON;
    private String body;

    HttpRequest(MockMvc mvc, String method, String uri) {
        this.mvc = mvc;
        this.method = method;
        this.uri = uri;
    }

    @SneakyThrows
    public HttpResult expect() {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(method, URI.create(uri)).headers(headers).contentType(contentType);
        if (body != null) {
            req.content(body);
        }
        ResultActions result = mvc.perform(req);
        return new HttpResult(result);
    }

    @SneakyThrows
    public HttpRequest withJson(Object any) {
        body = Mappers.JSON.serialize(any);
        this.contentType = MediaType.APPLICATION_JSON;
        return this;
    }

    public HttpRequest withTenant(String tenantId) {
        return header(Context.TENANT_ID, tenantId);
    }

    /*
    public HttpRequest withTrace(String spanId, String traceId) {
        header(Context.SPAN_ID, spanId);
        header(Context.TRACE_ID, traceId);
        return this;
    }*/

    public HttpRequest bearerAuth(String token) {
        header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return this;
    }

    public HttpRequest basicAuth(String username, String password) {
        header(HttpHeaders.AUTHORIZATION, HttpUtil.createBasicAuthorization(username, password));
        return this;
    }

    public HttpRequest header(String name, String value) {
        headers.put(name, Collections.singletonList(value));
        return this;
    }
}
