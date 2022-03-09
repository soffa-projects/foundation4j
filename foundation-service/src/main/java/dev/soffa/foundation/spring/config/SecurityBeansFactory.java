package dev.soffa.foundation.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeansFactory {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder createSpringPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public dev.soffa.foundation.commons.PasswordEncoder createPasswordEncoder(PasswordEncoder encoder) {
        return new dev.soffa.foundation.commons.PasswordEncoder() {

            @Override
            public String encode(String rawPassword) {
                return encoder.encode(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, String encryptedPassword) {
                return encoder.matches(rawPassword, encryptedPassword);
            }
        };
    }


}
