package dev.soffa.foundation.security;

import dev.soffa.foundation.models.Authentication;
import dev.soffa.foundation.models.Token;

public interface ClaimsExtractor {

    Authentication extractInfo(Token token);

}
