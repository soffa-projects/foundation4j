package dev.soffa.foundation;

import dev.soffa.foundation.commons.http.DefaultHttpClient;
import dev.soffa.foundation.commons.http.HttpResponse;
import dev.soffa.foundation.commons.http.HttpUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilTest {

    @SneakyThrows
    @Test
    public void testInterceptor() {

        HttpUtil.mockResponse((url, headers) -> {
            //EL
            return "devbox.local".equals(url.getHost());
        }, (url, headers) -> HttpResponse.ok("text/plain", "PONG"));
        DefaultHttpClient client = new DefaultHttpClient(HttpUtil.newOkHttpClient());
        HttpResponse res = client.get("https://devbox.local");
        assertEquals(200, res.getStatus());
        assertEquals("text/plain", res.getContentType());
        assertEquals("PONG", res.getBody());

        HttpUtil.mockResponse((url, headers) -> {
            //EL
            return "www.github.com".equals(url.getHost()) && "/hello".equals(url.getPath());
        }, (url, headers) -> HttpResponse.ok("application/json", "Hi"));

        client = new DefaultHttpClient(HttpUtil.newOkHttpClient());
        res = client.get("https://www.github.com/hello");
        assertEquals(200, res.getStatus());
        assertEquals("application/json", res.getContentType());
        assertTrue(res.getBody().contains("Hi"));
    }

}
