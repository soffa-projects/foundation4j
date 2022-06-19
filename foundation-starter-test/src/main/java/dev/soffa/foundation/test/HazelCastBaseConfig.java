package dev.soffa.foundation.test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public interface HazelCastBaseConfig {

    @Bean
    @ConditionalOnMissingBean
    default HazelcastInstance createInstance() {
        return Hazelcast.newHazelcastInstance();
    }
}
