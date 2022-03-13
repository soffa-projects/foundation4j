package dev.soffa.foundation;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.model.Authentication;
import dev.soffa.foundation.model.TokenType;
import dev.soffa.foundation.security.DefaultTokenProvider;
import dev.soffa.foundation.security.TokenProvider;
import dev.soffa.foundation.security.TokensConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenProviderTest {

    @Test
    public void testTokenProvider() {
        TokensConfig config = new TokensConfig("soffa", "XoEH&!TWrQ&T");
        TokenProvider tokenProvider = new DefaultTokenProvider(config);
        String token = tokenProvider.create(TokenType.JWT, "agent", ImmutableMap.of("liveMode", true)).getValue();
        Authentication auth = tokenProvider.decode(token);
        assertNotNull(auth);
        assertTrue(auth.isLiveMode());
    }

}
