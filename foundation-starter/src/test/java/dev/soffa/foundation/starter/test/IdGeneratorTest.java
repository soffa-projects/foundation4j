package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.commons.DistributedIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles({"test", "hazelcast"})
public class IdGeneratorTest {

    @Autowired
    DistributedIdGenerator generator;

    @Test
    public void test() {
        Set<String> uniq = new HashSet<>();
        assertNotNull(generator);
        for (int i = 0; i < 100_000; i++) {
            String id = generator.nextId( "tok_");
            if (uniq.contains(id)) {
                throw new IllegalStateException("Duplicate id: " + id);
            }
            uniq.add(id);
        }
    }
}
