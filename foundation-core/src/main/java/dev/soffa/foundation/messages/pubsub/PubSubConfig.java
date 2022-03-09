package dev.soffa.foundation.messages.pubsub;

import lombok.Data;

import java.util.Map;

@Data
public class PubSubConfig {

    private String broadcasting;
    private Map<String, PubSubClientConfig> clients;

}
