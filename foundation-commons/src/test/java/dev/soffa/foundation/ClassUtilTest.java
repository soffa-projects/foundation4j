package dev.soffa.foundation;

import dev.soffa.foundation.commons.ClassUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClassUtilTest {

    @Test
    public void testGeneric() {
        Type[] generics = ClassUtil.lookupGeneric(RepoAdapter.class, SimpleRepository.class);
        assertNotNull(generics);
        assertEquals(2, generics.length);
        assertEquals(Entity.class, generics[0]);
        assertEquals(String.class, generics[1]);

        generics = ClassUtil.lookupGeneric(RepoAdapter.class, EntityRepository.class);
        assertNotNull(generics);
        assertEquals(2, generics.length);
        assertEquals(Entity.class, generics[0]);
        assertEquals(String.class, generics[1]);

    }

    interface EntityRepository<A, N> {
    }

    interface Repo extends EntityRepository<Entity, String> {
    }

    static class Entity {
    }

    static class SimpleRepository<A, N> {
    }

    static class RepoAdapter extends SimpleRepository<Entity, String> implements Repo {
    }
}
