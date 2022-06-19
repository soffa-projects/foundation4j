package dev.soffa.foundation.data;

import dev.soffa.foundation.data.app.UserRepository;
import dev.soffa.foundation.test.BaseTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EntityRepositoryTest extends BaseTest {

    @Inject
    private UserRepository repository;
    ;

    @Test
    public void testRepository() {
        assertNotNull(repository);
    }
}
