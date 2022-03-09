package dev.soffa.foundation.test.spring.karate;

import com.google.common.collect.ImmutableMap;
import com.intuit.karate.core.Config;
import com.intuit.karate.core.ScenarioEngine;
import com.intuit.karate.http.HttpClient;
import com.intuit.karate.http.HttpClientFactory;
import com.intuit.karate.http.HttpRequest;
import com.intuit.karate.http.Response;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class MockMvcClient implements HttpClientFactory, HttpClient {

    private final MockMvc mvc;

    @Override
    public HttpClient create(ScenarioEngine engine) {
        return this;
    }

    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public void setConfig(Config config) {
        // Empty body
    }

    @SneakyThrows
    @Override
    public Response invoke(HttpRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (request.getHeaders() != null) {
            for (Map.Entry<String, List<String>> e : request.getHeaders().entrySet()) {
                headers.add(e.getKey(), e.getValue().get(0));
            }
        }
        String method = request.getMethod();
        URI uri = URI.create(request.getUrl());
        String contentType = "application/json";
        if (request.getContentType() != null) {
            contentType = request.getContentType();
        }
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(method, uri)
            .headers(headers)
            .contentType(contentType);

        if (request.getBody() != null) {
            req.content(request.getBody());
        }
        MvcResult result = mvc.perform(req).andReturn();

        return new Response(
            result.getResponse().getStatus(),
            ImmutableMap.of(),
            result.getResponse().getContentAsByteArray()
        );
    }
}
