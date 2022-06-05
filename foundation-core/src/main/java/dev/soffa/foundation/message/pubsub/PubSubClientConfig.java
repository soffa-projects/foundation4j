package dev.soffa.foundation.message.pubsub;

import dev.soffa.foundation.error.ConfigurationException;
import lombok.Data;

import java.util.Map;

@Data
public class PubSubClientConfig {

    private String addresses;
    private String username;
    private String password;
    private String subscribe;
    private String broadcasting;

    private Map<String, String> options;

    public void setSubjects(String subjects) {
        this.subscribe = subjects;
    }

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
