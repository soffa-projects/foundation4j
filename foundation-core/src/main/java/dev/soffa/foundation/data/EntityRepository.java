package dev.soffa.foundation.data;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnusedReturnValue")
public interface EntityRepository<E> {

    long count();

    default long count(Map<String, Object> filter) {
        return count(Criteria.of(filter));
    }

    long count(Criteria criteria);

    List<E> findAll();

    List<E> find(Criteria criteria);

    default List<E> find(Map<String, Object> filter) {
        return find(Criteria.of(filter));
    }

    Optional<E> get(Criteria criteria);

    default Optional<E> get(Map<String, Object> filter) {
        return get(Criteria.of(filter));
    }

    Optional<E> findById(Object value);

    E insert(E entity);

    E update(E entity);

    int delete(E entity);

    default int delete(Map<String, Object> filter) {
        return delete(Criteria.of(filter));
    }

    int delete(Criteria criteria);

    default boolean exists(Map<String, Object> filter) {
        return exists(Criteria.of(filter));
    }

    default boolean exists(Criteria criteria) {
        return count(criteria) > 0;
    }

    default boolean exists(String id) {
        return exists(ImmutableMap.of("id", id));
    }

}
