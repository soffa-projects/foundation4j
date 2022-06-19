package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.application.ID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
@ActiveProfiles({"test", "hazelcast"})
public class DefaultIdGeneratorTest {

    @Test
    public void test() {
        Set<String> uniq = new HashSet<>();
        for (int i = 0; i < 100_000; i++) {
            String id = ID.generate( "tok_");
            if (uniq.contains(id)) {
                throw new IllegalStateException("Duplicate id: " + id);
            }
            uniq.add(id);
        }
    }
}
