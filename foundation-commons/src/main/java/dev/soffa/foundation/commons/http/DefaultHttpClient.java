package dev.soffa.foundation.commons.http;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultHttpClient implements HttpClient {


    private final OkHttpClient client;

    public DefaultHttpClient() {
        client = HttpUtil.newOkHttpClient();
    }

    public DefaultHttpClient(OkHttpClient client) {
        this.client = client;
    }

    public static HttpClient newInstance() {
        return new DefaultHttpClient();
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



}
