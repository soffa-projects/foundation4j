package dev.soffa.foundation.spring.config.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import dev.soffa.foundation.commons.Hashids;
import dev.soffa.foundation.commons.IdGenerator;
import dev.soffa.foundation.config.AppConfig;
import org.apache.commons.lang3.StringUtils;

// @Component
//@ConditionalOnBean(HazelcastInstance.class)
//@Primary
public class HazelcastDistributedIdGenerator implements IdGenerator {

    private final FlakeIdGenerator idGenerator;
    private final Hashids hashids;

    public HazelcastDistributedIdGenerator(AppConfig appConfig, HazelcastInstance instance) {
        idGenerator = instance.getFlakeIdGenerator(appConfig.getName());
        hashids = new Hashids(appConfig.getName());
    }

    @Override
    public String nextId(String prefix) {
        long nextId = idGenerator.newId();
        return StringUtils.join(prefix, hashids.encode(nextId));
    }
}
