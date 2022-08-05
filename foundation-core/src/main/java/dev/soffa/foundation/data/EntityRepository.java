package dev.soffa.foundation.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.error.ResourceNotFoundException;
import dev.soffa.foundation.model.PagedList;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings({"UnusedReturnValue", "unused", "PMD.ExcessivePublicCount"})
public interface EntityRepository<E, I> {

    default long count() {
        return count(TenantId.CONTEXT);
    }

    long count(TenantId tenant);

    int execute(TenantId tenant, String command);

    default int execute(String command) {
        return execute(TenantId.CONTEXT, command);
    }

    default boolean isEmpty(TenantId tenant) {
        return count(tenant) == 0L;
    }

    default boolean isEmpty() {
        return isEmpty(TenantId.CONTEXT);
    }

    EntityRepository<E, I> withTenant(TenantId tenant);

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

    default Set<String> pluck(TenantId tenantId, String field) {
        return pluck(tenantId, field, 1, Integer.MAX_VALUE);
    }

    Set<String> pluck(TenantId tenantId, String field, int page, int count);

    default Set<String> pluck(String field) {
        return pluck(TenantId.CONTEXT, field);
    }

    default Set<String> pluck(String field, int page, int count) {
        return pluck(TenantId.CONTEXT, field, page, count);
    }

    default void pluckStream(String field, int page, int count, Consumer<Stream<String>> consumer) {
        pluckStream(TenantId.CONTEXT, field, page, count, consumer);
    }

    void pluckStream(TenantId tenant, String field, int page, int count, Consumer<Stream<String>> consumer);

    default PagedList<E> find(TenantId tenant, Criteria criteria) {
        return find(tenant, criteria, Paging.DEFAULT);
    }

    default PagedList<E> findAll(TenantId tenantId) {
        return findAll(tenantId, Paging.DEFAULT);
    }

    PagedList<E> find(TenantId tenant, Criteria criteria, Paging paging);

    DataStore getDataStore();

    int truncate(TenantId tenant);

    default int truncate() {
        return truncate(TenantId.CONTEXT);
    }

    default PagedList<E> find(Criteria criteria) {
        return find(criteria, Paging.DEFAULT);
    }

    default <T> List<T> query(String query, Class<T> resultClass) {
        return query(TenantId.CONTEXT, query, null, resultClass);
    }

    default <T> List<T> query(String query, Map<String,Object> binding, Class<T> resultClass) {
        return query(TenantId.CONTEXT, query, binding, resultClass);
    }

    <T> List<T> query(TenantId context, String query, Map<String,Object> binding, Class<T> resultClass);

    default PagedList<E> find(Criteria criteria, Paging paging) {
        return find(TenantId.CONTEXT, criteria, paging);
    }

    default PagedList<E> find(Map<String, Object> filter) {
        return find(filter, Paging.DEFAULT);
    }

    default void forEach(Map<String, Object> filter, int fetchSize, Consumer<E> consumer) {
        forEach(TenantId.CONTEXT, filter, fetchSize, consumer);
    }

    default void forEach(TenantId context, Map<String, Object> filter, int fetchSize, Consumer<E> consumer) {
        int page = 1;
        Preconditions.checkArgument(fetchSize >= 1);
        do {
            PagedList<E> res = find(context, Criteria.of(filter), new Paging(page++, fetchSize));
            for (E record : res.getData()) {
                consumer.accept(record);
            }
            if (!res.getPaging().isHasMore()) {
                break;
            }
        } while (true);
    }

    default PagedList<E> find(Map<String, Object> filter, Paging paging) {
        return find(Criteria.of(filter), paging);
    }

    default double sumBy(String field, Criteria criteria) {
        return sumBy(TenantId.CONTEXT, field, criteria);
    }

    default double sumBy(String field, Map<String, Object> criteria) {
        return sumBy(TenantId.CONTEXT, field, Criteria.of(criteria));
    }

    double sumBy(TenantId tenant, String field, Criteria criteria);

    default Optional<E> get(Criteria criteria) {
        return get(TenantId.CONTEXT, criteria);
    }


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

    default E insert(E entity) {
        return insert(TenantId.CONTEXT, entity);
    }

    default int[] insert(List<E> entities) {
        return insert(TenantId.CONTEXT, entities);
    }

    E insert(TenantId tenant, E entity);

    int[] insert(TenantId tenant, List<E> entities);

    E update(E entity, String... fields);

    String getTableName();

    default long loadCsvFile(File file, String delimiter) {
        return loadCsvFile(TenantId.CONTEXT, file, delimiter);
    }

    default long exportToCsvFile(File file, String query, String delimiter) {
        return exportToCsvFile(TenantId.CONTEXT, file, query, delimiter);
    }

    long exportToCsvFile(TenantId context, File file, String query, String delimiter);

    long loadCsvFile(TenantId tenant, File file, String delimiter);

    long loadCsvFile(TenantId tenant, InputStream source, String delimiter);

    E update(TenantId tenant, E entity, String... fields);

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

    TenantId resolveTenant(TenantId tenant);

}
