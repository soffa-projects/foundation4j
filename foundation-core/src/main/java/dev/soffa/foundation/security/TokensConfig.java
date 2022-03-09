package dev.soffa.foundation.security;


import dev.soffa.foundation.commons.TextUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokensConfig {

    private String issuer;
    private String secret;
    private String publicJwks;
    private String privateJwks;
    private int defaultTtl = 60;

    public TokensConfig(String issuer, String secret) {
        this.issuer = issuer;
        this.secret = secret;
    }

    public boolean isValid() {
        return TextUtil.isNotEmpty(secret) || TextUtil.isNotEmpty(publicJwks) || TextUtil.isNotEmpty(privateJwks);
    }

}
