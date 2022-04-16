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

        HttpUtil.loadMocks("/http-mocks.yml");

        DefaultHttpClient client = DefaultHttpClient.getInstance();

        HttpResponse res = client.get("https://devbox.local");
        assertEquals(200, res.getStatus());
        assertEquals("text/plain", res.getContentType());
        assertEquals("PONG", res.getBody()); // Because it's a JSON response

        res = client.get("https://www.github.com/hello");
        assertEquals(200, res.getStatus());
        assertEquals("application/json", res.getContentType());
        assertTrue(res.getBody().contains("Hi"));
    }

}
