package dev.soffa.foundation.starter.test;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.model.Authentication;
import dev.soffa.foundation.model.Token;
import dev.soffa.foundation.model.TokenType;
import dev.soffa.foundation.security.DefaultTokenProvider;
import dev.soffa.foundation.security.TokenProvider;
import dev.soffa.foundation.security.TokensConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TokenProviderTest {

    @Test
    public void testJwtEncoder() {
        TokensConfig config = new TokensConfig("test", "yujRAkZLDBW*Xaw3");
        TokenProvider tokens = new DefaultTokenProvider(config);

        Token token = tokens.create(TokenType.JWT, "Foundation", ImmutableMap.of("email", "foundation@soffa.io"));
        assertNotNull(token.getValue());
        assertEquals("Foundation", token.getSubject());

        Authentication auth = tokens.decode(token.getValue());
        assertNotNull(auth);
    }

}
