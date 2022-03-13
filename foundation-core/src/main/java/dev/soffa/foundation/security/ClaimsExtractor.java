package dev.soffa.foundation.security;

import dev.soffa.foundation.model.Authentication;
import dev.soffa.foundation.model.Token;

public interface ClaimsExtractor {

    Authentication extractInfo(Token token);

}
