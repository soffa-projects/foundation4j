package dev.soffa.foundation.commons.http.mocks;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.soffa.foundation.commons.RandomUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.http.HttpHeaders;
import dev.soffa.foundation.commons.http.HttpResponse;
import dev.soffa.foundation.commons.http.HttpResponseProvider;
import dev.soffa.foundation.errors.TechnicalException;
import dev.soffa.foundation.models.ResponseStatus;
import lombok.Data;

import java.net.URL;
import java.util.List;
import java.util.Map;

@Data
public class HttpMock implements HttpResponseProvider {

    private String host;
    private String path;
    @JsonProperty("content-type")
    private String contentType;
    private Map<String, String> headers;
    private List<HttpMockResponse> response;

    @Override
    public HttpResponse apply(URL url, HttpHeaders headers) {
        return getResponse();
    }

    @Data
    public static class HttpMockResponse {
        @JsonProperty("content-type")
        private String contentType;
        private String body;
        private int status = ResponseStatus.OK;

        public int getStatus() {
            if (status <= 0) {
                return ResponseStatus.OK;
            }
            return status;
        }
    }

    public boolean matches(URL url, HttpHeaders headers) {
        if (TextUtil.isNotEmpty(host) && !host.equalsIgnoreCase(url.getHost())) {
            return false;
        }
        if (TextUtil.isNotEmpty(path) && !path.equalsIgnoreCase(url.getPath())) {
            return false;
        }
        if (this.headers != null) {
            for (Map.Entry<String, String> e : this.headers.entrySet()) {
                if (!headers.equals(e.getKey(), e.getValue())) {
                    return false;
                }
            }
        }
        //noinspection RedundantIfStatement
        if (TextUtil.isNotEmpty(contentType) && !headers.contentTypeIs(contentType)) {
            return false;
        }
        // All conditions are met, so we can return true
        return true;
    }

    public HttpResponse getResponse() {
        if (response == null || response.isEmpty()) {
            throw new TechnicalException("No response found for mock: %s", host);
        }
        int index = RandomUtil.nextInt(0, response.size() - 1);
        HttpMockResponse response = this.response.get(index);

        return HttpResponse.builder()
            .status(response.getStatus())
            .contentType(response.getContentType())
            .body(response.getBody()).build();
    }
}
