package dev.soffa.foundation.application;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.models.Authentication;
import dev.soffa.foundation.models.Token;
import dev.soffa.foundation.models.TokenType;
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
