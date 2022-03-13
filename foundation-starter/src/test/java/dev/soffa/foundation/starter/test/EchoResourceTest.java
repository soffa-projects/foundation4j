package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.starter.test.app.EchoResource;
import dev.soffa.foundation.starter.test.app.operation.Echo;
import dev.soffa.foundation.starter.test.app.operation.EchoInput;
import dev.soffa.foundation.test.spring.HttpExpect;
import lombok.SneakyThrows;
import org.checkerframework.com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class EchoResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EchoResource resource;

    @Autowired
    private Echo echoUseCase;

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

        client.patch("/v1/messages/123456")
            .withJson(ImmutableMap.of("content", content))
            .expect().isCreated().json("content", "123456/" + content);

        client.patch("/v1.1/messages/123456")
            .expect().isOK().json("content", "123456");

        client.get("/v1.1/messages")
            .expect().isOK();

        client.get("/v1.2/messages")
            .expect().isOK();

    }

}
