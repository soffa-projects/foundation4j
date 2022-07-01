package dev.soffa.foundation.data;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.error.ResourceNotFoundException;
import dev.soffa.foundation.model.PagedList;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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

    default PagedList<E> findAll() {
        return findAll(Paging.DEFAULT);
    }

    default PagedList<E> findAll(Paging paging) {
        return findAll(TenantId.CONTEXT, paging);
    }

    PagedList<E> findAll(TenantId tenantId, Paging paging);

    default PagedList<E> find(TenantId tenant, Criteria criteria) {
        return find(tenant, criteria, Paging.DEFAULT);
    }


    default PagedList<E> findAll(TenantId tenantId) {
        return findAll(tenantId, Paging.DEFAULT);
    }

    PagedList<E> find(TenantId tenant, Criteria criteria, Paging paging);

    default PagedList<E> find(Criteria criteria) {
        return find(criteria, Paging.DEFAULT);
    }

    default PagedList<E> find(Criteria criteria, Paging paging) {
        return find(TenantId.CONTEXT, criteria, paging);
    }

    default PagedList<E> find(Map<String, Object> filter) {
        return find(filter, Paging.DEFAULT);
    }

    default PagedList<E> find(Map<String, Object> filter, Paging paging) {
        return find(Criteria.of(filter), paging);
    }

    Optional<E> get(Criteria criteria);

    default double sumBy(String field, Criteria criteria) {
        return sumBy(TenantId.CONTEXT, field, criteria);
    }

    default double sumBy(String field, Map<String,Object> criteria) {
        return sumBy(TenantId.CONTEXT, field, Criteria.of(criteria));
    }

    double sumBy(TenantId tenant, String field, Criteria criteria);

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

    default void useTransaction(Consumer<EntityRepository<E, I>> consumer) {
        useTransaction(TenantId.CONTEXT, consumer);
    }

    void useTransaction(TenantId tenant, Consumer<EntityRepository<E, I>> consumer);

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
