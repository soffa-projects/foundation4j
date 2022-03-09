package dev.soffa.foundation.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnusedReturnValue")
public interface EntityRepository<E> {

    long count();

    long count(String where, Map<String, Object> binding);

    List<E> findAll();

    List<E> find(String where, Map<String, Object> binding);

    Optional<E> get(String where, Map<String, Object> binding);

    Optional<E> findById(Object value);

    E insert(E entity);

    E update(E entity);

    int delete(E entity);

    int delete(String where, Map<String, Object> binding);
}
