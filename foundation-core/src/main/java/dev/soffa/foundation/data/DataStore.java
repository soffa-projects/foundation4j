package dev.soffa.foundation.data;


import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.model.PagedList;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface DataStore {


    default <E> Optional<E> findById(Class<E> entityClass, Object value) {
        return findById(TenantId.CONTEXT, entityClass, value);
    }

    boolean ping();

    <E> Optional<E> findById(TenantId tenant, Class<E> entityClass, Object value);

    default <E> long count(Class<E> entityClass) {
        return count(TenantId.CONTEXT, entityClass);
    }

    <E> long count(TenantId tenant, Class<E> entityClass);

    default <E> long count(@NonNull Class<E> entityClass, @NonNull Criteria criteria) {
        return count(TenantId.CONTEXT, entityClass, criteria);
    }

    <E> long count(TenantId tenant, @NonNull Class<E> entityClass, Criteria criteria);

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

    String getTablesPrefix();

    default String getTableName(String name) {
        return TextUtil.join("_", getTablesPrefix(), name);
    }

    <E> int[] insert(TenantId tenant, List<E> entities);

    default <E> E update(E entity, String... fields) {
        return update(TenantId.CONTEXT, entity, fields);
    }

    <E> E update(TenantId tenant, E entity, String... fields);

    default <E> int delete(E entity) {
        return delete(TenantId.CONTEXT, entity);
    }

    <E> int delete(TenantId tenant, E entity);

    default <E> PagedList<E> find(Class<E> entityClass, Criteria criteria) {
        return find(TenantId.CONTEXT, entityClass, criteria, Paging.DEFAULT);
    }

    <E> PagedList<E> find(TenantId tenant, Class<E> entityClass, Criteria criteria, Paging paging);

    default <E> PagedList<E> find(Class<E> entityClass, Criteria criteria, Paging paging) {
        return find(TenantId.CONTEXT, entityClass, criteria, paging);
    }

    default int execute(String command) {
        return execute(TenantId.CONTEXT, command);
    }

    int execute(TenantId tenant, String command);

    default <E> PagedList<E> find(TenantId tenant, Class<E> entityClass, Criteria criteria) {
        return find(tenant, entityClass, criteria, Paging.DEFAULT);
    }

    default <E> Optional<E> get(Class<E> entityClass, Criteria criteria) {
        return get(TenantId.CONTEXT, entityClass, criteria);
    }

    <E> Optional<E> get(TenantId tenant, Class<E> entityClass, Criteria criteria);

    default <E> PagedList<E> findAll(Class<E> entityClass) {
        return findAll(TenantId.CONTEXT, entityClass);
    }

    default <E> PagedList<E> findAll(TenantId tenant, Class<E> entityClass) {
        return findAll(tenant, entityClass, Paging.DEFAULT);
    }


    <E> PagedList<E> findAll(TenantId tenant, Class<E> entityClass, Paging paging);

    <T> List<T> query(String query, Class<T> resultClass);

    void useTransaction(TenantId tenant, Consumer<DataStore> consumer);

    <E> double sumBy(TenantId tenant, Class<E> entityClass, String field, Criteria criteria);

    <E> Set<String> pluck(TenantId tenant, Class<E> entityClass, String field, int page, int count);

    <T> void pluckStream(TenantId tenant, Class<T> entityClass, String field, int page, int count, Consumer<Stream<String>> consumer);

    long loadCsvFile(TenantId tenant, String tableName, File file, String delimiter);

    long loadCsvFile(TenantId tenant, String tableName, InputStream input, String delimiter);

    long exportToCsvFile(TenantId tenant, String tableName, String query, File file, String delimiter);

    <E> int truncate(TenantId resolveTenant, Class<E> entityClass);

}
