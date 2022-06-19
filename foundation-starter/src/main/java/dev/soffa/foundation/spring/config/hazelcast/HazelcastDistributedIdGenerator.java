package dev.soffa.foundation.spring.config.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import dev.soffa.foundation.application.ID;
import dev.soffa.foundation.commons.Hashids;
import dev.soffa.foundation.commons.IdGenerator;
import dev.soffa.foundation.config.AppConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
//@ConditionalOnBean(HazelcastInstance.class)
@Primary
public class HazelcastDistributedIdGenerator implements IdGenerator {

    private final FlakeIdGenerator idGenerator;
    private final Hashids hashids;

    public HazelcastDistributedIdGenerator(AppConfig appConfig, HazelcastInstance instance) {
        idGenerator = instance.getFlakeIdGenerator(appConfig.getName());
        hashids = new Hashids(appConfig.getName());
        ID.generator = this;
    }

    @Override
    public String nextId(String prefix) {
        long nextId = idGenerator.newId();
        return StringUtils.join(prefix, hashids.encode(nextId));
    }
}
