package dev.soffa.foundation.messages.pubsub;

import dev.soffa.foundation.errors.ConfigurationException;
import lombok.Data;

import java.util.Map;

@Data
public class PubSubClientConfig {

    private String addresses;
    private String username;
    private String password;
    private String subjects;
    private String broadcasting;

    private Map<String, String> options;

    public void afterPropertiesSet() {
        if (addresses == null) {
            throw new ConfigurationException("addresses is required");
        }
    }

    public String getOption(String name) {
        if (options == null || !options.containsKey(name)) {
            return null;
        }
        return options.get(name);
    }

}
