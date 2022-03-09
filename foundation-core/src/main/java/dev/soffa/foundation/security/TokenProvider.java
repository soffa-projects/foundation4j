package dev.soffa.foundation.security;


import dev.soffa.foundation.models.Authentication;
import dev.soffa.foundation.models.Token;
import dev.soffa.foundation.models.TokenType;

import java.util.Map;

public interface TokenProvider {

    default Token create(TokenType type, String subject) {
        return create(type, subject, null);
    }

    Token create(TokenType type, String subject, Map<String, Object> claims);

    Token create(TokenType type, String subject, Map<String, Object> claims, int ttlInMinutes);

    Authentication decode(String token, ClaimsExtractor extractor);

    Authentication decode(String token);

    Authentication extractInfo(Token token);

    TokensConfig getConfig();

}
