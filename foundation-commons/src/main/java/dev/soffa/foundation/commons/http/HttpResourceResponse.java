package dev.soffa.foundation.commons.http;

import dev.soffa.foundation.commons.IOUtil;
import lombok.SneakyThrows;

import java.net.URL;

public class HttpResourceResponse implements HttpResponseProvider {

    private final String location;
    private String contentType = "application/json";


    @SneakyThrows
    public HttpResourceResponse(String location) {
        this.location = location;
        if (location.endsWith(".xml")) {
            contentType = "text/xml";
        }
    }

    @Override
    public HttpResponse apply(URL url, HttpHeaders headers) {
        return HttpResponse.ok(contentType, IOUtil.getResourceAsString(location));
    }
}
