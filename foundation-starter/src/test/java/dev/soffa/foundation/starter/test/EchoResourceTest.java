package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.model.Token;
import dev.soffa.foundation.model.TokenType;
import dev.soffa.foundation.security.TokenProvider;
import dev.soffa.foundation.starter.test.app.ApplicationListener;
import dev.soffa.foundation.starter.test.app.handlers.Echo;
import dev.soffa.foundation.starter.test.app.model.EchoInput;
import dev.soffa.foundation.starter.test.app.resource.EchoResource;
import dev.soffa.foundation.test.spring.HttpExpect;
import lombok.SneakyThrows;
import org.checkerframework.com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "jackson.property-naming-strategy=SNAKE_CASE")
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class EchoResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EchoResource resource;

    @Autowired
    private Echo echoUseCase;
    @Autowired
    private TokenProvider tokenProvider;

    @Test
    public void testListener() {
        Assertions.assertTrue(ApplicationListener.onApplicationReadyCalled.get());
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void testDynamicResource() {
        assertNotNull(resource);
        assertNotNull(echoUseCase);

        HttpExpect client = new HttpExpect(mockMvc);
        client.get("/v3/api-docs")
                .expect().isOK().hasJson("paths./v1/echo");

        String content = "Hello World!";

        String requestBody = Mappers.JSON.serialize(new EchoInput(content));
        EchoInput input = Mappers.JSON.deserialize(requestBody, EchoInput.class);
        assertNotNull(input);
        assertNotNull(input.getMessage());
        assertEquals(content, input.getMessage());

        client.post("/v1/echo")
                .withJson(input)
                .expect().isOK().json("content", content);

        client.get("/v1/echo")
                .withJson(input)
                .expect().isOK().json(jsonExpect -> {
                    jsonExpect.eq("content", "Echo");
                    jsonExpect.exists("message_id");
                    jsonExpect.exists("links.self.href");
                });

        client.patch("/v1/messages/123456")
                .withJson(ImmutableMap.of("content", content))
                .expect().isCreated().json("$.content", "123456/" + content);

        client.patch("/v1.1/messages/123456")
                .expect().isOK().json("content", "123456");

        client.get("/v1.1/messages")
                .expect().isOK();

        client.get("/v1.2/messages")
                .expect().isOK();
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void testSecuredAnnotation() {
        HttpExpect client = new HttpExpect(mockMvc);

        String content = "Hello World!";

        String requestBody = Mappers.JSON.serialize(new EchoInput(content));
        EchoInput input = Mappers.JSON.deserialize(requestBody, EchoInput.class);
        Token bearerToken = tokenProvider.create(
                TokenType.JWT, "user",
                ImmutableMap.of("permissions", "foo")
        );
        client.post("/v1/echo/secured")
                .bearerAuth(bearerToken.getValue())
                .withJson(input)
                .expect().isForbidden();

        bearerToken = tokenProvider.create(
                TokenType.JWT, "user",
                ImmutableMap.of("permissions", "admin")
        );
        client.post("/v1/echo/secured")
                .bearerAuth(bearerToken.getValue())
                .withJson(input)
                .expect().isOK();

    }

}
