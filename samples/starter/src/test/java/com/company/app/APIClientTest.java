package com.company.app;

import com.company.app.core.PingResponse;
import com.company.app.gateways.API;
import dev.soffa.foundation.client.RestClient;
import dev.soffa.foundation.context.Context;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class APIClientTest {

    @LocalServerPort
    private int port;

    @SneakyThrows
    @Test
    public void testAPIClient() {
        API client = RestClient.newInstance(API.class, "http://localhost:" + port);
        Context ctx = Context.create("T1");
        PingResponse response = client.ping(ctx);
        assertEquals("PONG", response.getValue());
        assertEquals("Hello", client.echo("Hello", ctx));
    }


}
