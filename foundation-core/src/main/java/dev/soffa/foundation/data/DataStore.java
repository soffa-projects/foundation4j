package dev.soffa.foundation.data;


import dev.soffa.foundation.models.TenantId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DataStore {

    default <E> Optional<E> findById(Class<E> entityClass, Object value) {
        return findById(TenantId.INHERIT, entityClass, value);
    }

    <E> Optional<E> findById(TenantId tenant, Class<E> entityClass, Object value);

    default <E> long count(Class<E> entityClass) {
        return count(TenantId.INHERIT, entityClass);
    }

    <E> long count(TenantId tenant, Class<E> entityClass);

    default <E> long count(@NonNull Class<E> entityClass, @NonNull String where, Map<String, Object> binding) {
        return count(TenantId.INHERIT, entityClass, where, binding);
    }

    <E> long count(TenantId tenant, @NonNull Class<E> entityClass, @NonNull String where, Map<String, Object> binding);

    default <E> int delete(@NonNull Class<E> entityClass, @NonNull String where, Map<String, Object> binding) {
        return delete(TenantId.INHERIT, entityClass, where, binding);
    }

    <E> int delete(TenantId tenant, @NonNull Class<E> entityClass, @NonNull String where, Map<String, Object> binding);

    default <E> E insert(E entity) {
        return insert(TenantId.INHERIT, entity);
    }

    <E> E insert(TenantId tenant, E entity);

    default <E> E update(E entity) {
        return update(TenantId.INHERIT, entity);
    }

    <E> E update(TenantId tenant, E entity);

    default <E> int delete(E entity) {
        return delete(TenantId.INHERIT, entity);
    }

    <E> int delete(TenantId tenant, E entity);

    default <E> List<E> find(Class<E> entityClass, String where, Map<String, Object> binding) {
        return find(TenantId.INHERIT, entityClass, where, binding);
    }

    <E> List<E> find(TenantId tenant, Class<E> entityClass, String where, Map<String, Object> binding);

    default <E> Optional<E> get(Class<E> entityClass, String where, Map<String, Object> binding) {
        return get(TenantId.INHERIT, entityClass, where, binding);
    }

    <E> Optional<E> get(TenantId tenant, Class<E> entityClass, String where, Map<String, Object> binding);

    default <E> List<E> findAll(Class<E> entityClass) {
        return findAll(TenantId.INHERIT, entityClass);
    }

    <E> List<E> findAll(TenantId tenant, Class<E> entityClass);
}
