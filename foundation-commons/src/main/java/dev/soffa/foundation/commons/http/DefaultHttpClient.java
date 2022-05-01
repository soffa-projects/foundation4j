package dev.soffa.foundation.commons.http;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultHttpClient implements HttpClient {


    private static DefaultHttpClient defaultInstance = new DefaultHttpClient();
    private final OkHttpClient client;

    public DefaultHttpClient() {
        client = HttpUtil.newOkHttpClient();
    }

    public DefaultHttpClient(OkHttpClient client) {
        this.client = client;
    }

    public static DefaultHttpClient getInstance() {
        return defaultInstance;
    }

    public void setDefaultInstance(DefaultHttpClient instance) {
        defaultInstance = instance;
    }

    @SneakyThrows
    @Override
    public HttpResponse request(HttpRequest req) {
        RequestBody body = null;

        if (req.getBody() != null) {
            Object b = req.getBody();
            if (b instanceof String) {
                body = RequestBody.create((String) b, MediaType.parse(req.getContentType()));
            } else if (req.getContentType() == null || req.getContentType().contains("json")) {
                body = RequestBody.create(Mappers.JSON.serialize(req.getBody()), MediaType.parse(req.getContentType()));
            } else {
                throw new TechnicalException("Content type not supported: %s", req.getContentType());
            }
        }

        Map<String, String> hds = Optional.ofNullable(req.getHeaders()).orElse(new HashMap<>());
        hds.put("Content-Type", req.getContentType());
        Headers headers = Headers.of(hds);
        Request request = new Request.Builder()
            .url(req.getUrl())
            .method(req.getMethod(), body)
            .headers(headers)
            .build();
        Call call = client.newCall(request);
        try (Response result = call.execute()) {
            HttpResponse.HttpResponseBuilder res = HttpResponse.builder()
                .status(result.code())
                .message(result.message());
            try (ResponseBody responseBody = result.body()) {
                if (responseBody != null) {
                    MediaType contenType = responseBody.contentType();
                    if (contenType != null) {
                        res.contentType(contenType.type() + "/" + contenType.subtype());
                    }
                    res.body(responseBody.string());
                }
            }
            return res.build();
        }
    }


    /*
    public HttpResponse get(String url) {
        return this.request(HttpRequest.get(url));
    }

    public HttpResponse post(String url) {
        return request(HttpRequest.post(url));
    }

    public HttpResponse post(String url, Object body) {
        return request(HttpRequest.post(url, body));
    }

    public HttpResponse put(String url) {
        return request(HttpRequest.put(url));
    }

    public HttpResponse put(String url, Object body) {
        return request(HttpRequest.put(url, body));
    }

    public HttpResponse patch(String url) {
        return request(HttpRequest.patch(url));
    }

    public HttpResponse patch(String url, Object body) {
        return request(HttpRequest.patch(url, body));
    }

    public HttpResponse delete(String url) {
        return request(HttpRequest.delete(url));
    }

    public HttpResponse delete(String url, Object body) {
        return request(HttpRequest.delete(url, body));
    }


     */

}
