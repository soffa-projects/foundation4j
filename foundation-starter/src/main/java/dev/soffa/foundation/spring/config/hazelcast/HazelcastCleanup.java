package dev.soffa.foundation.spring.config.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
@AllArgsConstructor
public class HazelcastCleanup {

    private final HazelcastInstance instance;

    @PreDestroy
    public void cleanup() {
        instance.shutdown();
    }
}
