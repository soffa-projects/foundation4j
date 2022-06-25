package dev.soffa.foundation.data;


import dev.soffa.foundation.model.TenantId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;

public interface DataStore {

    default <E> Optional<E> findById(Class<E> entityClass, Object value) {
        return findById(TenantId.CONTEXT, entityClass, value);
    }

    <E> Optional<E> findById(TenantId tenant, Class<E> entityClass, Object value);

    default <E> long count(Class<E> entityClass) {
        return count(TenantId.CONTEXT, entityClass);
    }

    <E> long count(TenantId tenant, Class<E> entityClass);

    default <E> long count(@NonNull Class<E> entityClass, @NonNull Criteria criteria) {
        return count(TenantId.CONTEXT, entityClass, criteria);
    }

    <E> long count(TenantId tenant, @NonNull Class<E> entityClass, @NonNull Criteria criteria);

    default <E> int delete(@NonNull Class<E> entityClass, @NonNull Criteria criteria) {
        return delete(TenantId.CONTEXT, entityClass, criteria);
    }

    <E> int delete(TenantId tenant, @NonNull Class<E> entityClass, @NonNull Criteria criteria);

    default <E> E insert(E entity) {
        return insert(TenantId.CONTEXT, entity);
    }

    default <E> int[] batch(List<E> entity) {
        return batch(TenantId.CONTEXT, entity);
    }

    <E> int[] batch(TenantId tenant, List<E> entity);

    default <E> int[] batch(String table, List<E> entity) {
        return batch(TenantId.CONTEXT, table, entity);
    }

    <E> int[] batch(TenantId tenantId, String table, List<E> entity);

    <E> E insert(TenantId tenant, E entity);

    default <E> E update(E entity) {
        return update(TenantId.CONTEXT, entity);
    }

    <E> E update(TenantId tenant, E entity);

    default <E> int delete(E entity) {
        return delete(TenantId.CONTEXT, entity);
    }

    <E> int delete(TenantId tenant, E entity);

    default <E> List<E> find(Class<E> entityClass, Criteria criteria) {
        return find(TenantId.CONTEXT, entityClass, criteria);
    }

    default int execute(String command) {
        return execute(TenantId.CONTEXT, command);
    }

    int execute(TenantId tenant, String command);

    <E> List<E> find(TenantId tenant, Class<E> entityClass, Criteria criteria);

    default <E> Optional<E> get(Class<E> entityClass, Criteria criteria) {
        return get(TenantId.CONTEXT, entityClass, criteria);
    }

    <E> Optional<E> get(TenantId tenant, Class<E> entityClass, Criteria criteria);

    default <E> List<E> findAll(Class<E> entityClass) {
        return findAll(TenantId.CONTEXT, entityClass);
    }

    <E> List<E> findAll(TenantId tenant, Class<E> entityClass);

    <T> List<T> query(String query, Class<T> resultClass);

}
