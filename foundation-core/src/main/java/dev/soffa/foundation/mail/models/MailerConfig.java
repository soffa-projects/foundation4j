package dev.soffa.foundation.mail.models;

import dev.soffa.foundation.commons.TextUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MailerConfig {

    private int port = 25;
    private String provider;
    private String hostname;
    private String username;
    private String password;
    private String sender;
    private boolean tls;

    public MailerConfig(String provider, String hostname, String username, String password) {
        this.provider = provider;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    public boolean hasCredentials() {
        return TextUtil.isNotEmpty(username);
    }

    public void afterPropertiesSet() {
        if (TextUtil.isEmpty(hostname)) {
            return;
        }
        if (hostname.contains(":")) {
            String[] parts = hostname.split(":");
            hostname = parts[0];
            port = Integer.parseInt(parts[1]);
        }
    }
}
