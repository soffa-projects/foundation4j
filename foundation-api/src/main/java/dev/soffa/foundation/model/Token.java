package dev.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    private String type = "JWT";
    private String value;
    private String subject;
    private Map<String, Object> claims;

    /**
     * Duration in minutes
     */
    private int expiresIn;

    public Token(String value) {
        this.value = value;
    }

    public Token(String value, String subject, Map<String, Object> claims) {
        this(value);
        this.subject = subject;
        this.claims = claims;
    }

    public Token(String value, String subject, Map<String, Object> claims, int expiresIn) {
        this(value, subject, claims);
        this.expiresIn = expiresIn;
    }

    public Optional<String> lookupClaim(String... candidates) {
        if (claims == null || claims.isEmpty()) {
            return Optional.empty();
        }

        for (String claim : claims.keySet()) {
            for (String candidate : candidates) {
                if (claim.equalsIgnoreCase(candidate)) {
                    Object value = claims.get(claim);
                    if (value != null) {
                        return Optional.of(value.toString());
                    }
                }
            }
        }
        return Optional.empty();
    }

    public int getExpiresInSeconds() {
        return expiresIn * 60;
    }

}



