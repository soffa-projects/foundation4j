package dev.soffa.foundation.commons;

public interface PasswordEncoder {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String encryptedPassword);
}
