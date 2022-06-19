package dev.soffa.foundation.spring.config.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.AppConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {

    private static final Logger LOG = Logger.get(HazelcastConfiguration.class);

    @Bean
    @ConfigurationProperties(prefix = "app.hazelcast")
    public HazelCastProps createCondifgModel() {
        return new HazelCastProps();
    }

    @Bean
    public HazelcastInstance createInstance(AppConfig appConfig, HazelCastProps props) {
        if (TextUtil.isEmpty(props.getServers()) || "embedded".equalsIgnoreCase(props.getServers())) {
            LOG.info("Using embedded Hazelcast instance");
            Config config = new Config(appConfig.getName());
            return Hazelcast.newHazelcastInstance(config);
        } else {
            LOG.info("Configuration HazelCast instance");
            ClientConfig config = new ClientConfig();
            config.setInstanceName(appConfig.getName());
            for (String server : props.getServers().split(",")) {
                config.getNetworkConfig().addAddress(server.trim());
            }
            return HazelcastClient.newHazelcastClient(config);
        }

    }

}
