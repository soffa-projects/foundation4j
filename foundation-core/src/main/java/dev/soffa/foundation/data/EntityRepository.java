package dev.soffa.foundation.data;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.error.ResourceNotFoundException;
import dev.soffa.foundation.model.TenantId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface EntityRepository<E, I> {

    long count();

    default long count(Map<String, Object> filter) {
        return count(Criteria.of(filter));
    }

    default long count(Criteria criteria) {
        return count(TenantId.CONTEXT, criteria);
    }
    long count(TenantId tennant, Criteria criteria);

    default List<E> findAll() {
        return findAll(TenantId.CONTEXT);
    }

    List<E> findAll(TenantId tenantId);

    List<E> find(TenantId tenant, Criteria criteria);

    default List<E> find(Criteria criteria) {
        return find(TenantId.CONTEXT, criteria);
    }

    default List<E> find(Map<String, Object> filter) {
        return find(Criteria.of(filter));
    }

    Optional<E> get(Criteria criteria);

    Optional<E> get(TenantId tenant, Criteria criteria);

    default Optional<E> get(Map<String, Object> filter) {
        return get(Criteria.of(filter));
    }

    default Optional<E> get(TenantId tenant, Map<String, Object> filter) {
        return get(tenant, Criteria.of(filter));
    }

    default E get(I id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource does not exists"));
    }

    Optional<E> findById(I id);

    Optional<E> findById(TenantId tenant, I id);

    E insert(E entity);

    E insert(TenantId tenant, E entity);

    E update(E entity);

    E update(TenantId tenant, E entity);

    int delete(E entity);

    int delete(TenantId tenant, E entity);

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

    default boolean exists(I id) {
        return exists(ImmutableMap.of("id", id));
    }


}
