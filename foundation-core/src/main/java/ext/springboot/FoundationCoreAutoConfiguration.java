package ext.springboot;

import dev.soffa.foundation.security.DefaultTokenProvider;
import dev.soffa.foundation.security.TokenProvider;
import dev.soffa.foundation.security.TokensConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoundationCoreAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "app.security.tokens")
    public TokensConfig createSecurityConfig() {
        return new TokensConfig();
    }

    @Bean
    public TokenProvider createJwtEncoder(TokensConfig config) {
        if (config.isValid()) {
            return new DefaultTokenProvider(config);
        }
        return null;
    }

}
