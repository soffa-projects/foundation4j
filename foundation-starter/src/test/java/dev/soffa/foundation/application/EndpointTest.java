package dev.soffa.foundation.application;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.models.Token;
import dev.soffa.foundation.models.TokenType;
import dev.soffa.foundation.security.TokenProvider;
import dev.soffa.foundation.test.spring.HttpExpect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EndpointTest {

    private final HttpExpect test;
    private final TokenProvider tokens;

    public EndpointTest(@Autowired MockMvc mvc, @Autowired TokenProvider tokens) {
        this.tokens = tokens;
        this.test = new HttpExpect(mvc);
    }

    @Test
    public void testPublicEndpoint() {
        test.get("/public").expect().isOK();
    }

    @Test
    public void testInvalidJwt() {
        test.get("/secure").bearerAuth("invalid jwt").expect().isUnauthorized();
    }

    @Test
    public void testSecureEndpoint() {
        test.get("/secure").expect().isUnauthorized();

        Token token = tokens.create(TokenType.JWT, "user", ImmutableMap.of(
                "application", "AppName",
                "tenant", "T1"
            )
        );
        test.get("/secure").bearerAuth(token.getValue()).expect().isOK();

        test.get("/secure/full")
            .bearerAuth(token.getValue())
            .header("X-ApplicationName", "TEST")
            .expect().isOK();

        test.get("/secure/full")
            .bearerAuth(token.getValue())
            .header("X-TenantId", "BF")
            .expect().isOK();

        test.get("/secure/full")
            .bearerAuth(token.getValue())
            .header("X-ApplicationName", "App")
            .header("X-TenantId", "TX01")
            .expect().isOK();

    }

}
