package dev.soffa.foundation.data;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.jdbi.DBHandleProvider;
import dev.soffa.foundation.model.PagedList;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SimpleRepository<E, I> implements EntityRepository<E, I> {

    public static final String PH_TABLE = "<table>";

    private final DataStore ds;
    private final Class<E> entityClass;
    private final String tableName;
    private TenantId lockedTenant = TenantId.CONTEXT;

    public SimpleRepository(DataStore ds, Class<E> entityClass) {
        this(ds, entityClass, null);
    }

    public SimpleRepository(DataStore ds, Class<E> entityClass, String tableName) {
        this(ds, entityClass, tableName, null);
    }

    public SimpleRepository(String databaseUrl, Class<E> entityClass, String tableName) {
        this(SimpleDataStore.create(databaseUrl), entityClass, tableName, null);
    }

    public SimpleRepository(DataStore ds, Class<E> entityClass, String tableName, String tenant) {
        this.ds = ds;
        this.entityClass = entityClass;
        this.tableName = tableName;

        if (TextUtil.isNotEmpty(tableName)) {
            EntityInfo.registerTable(entityClass, tableName);
        }
        if (TextUtil.isNotEmpty(tenant)) {
            lockedTenant = TenantId.of(tenant);
        }
    }


    public SimpleRepository(DB db, Class<E> entityClass, String tableName, String tenant) {
        this(new SimpleDataStore(new DBHandleProvider(db), db.getTablesPrefix()), entityClass, tableName, tenant);
    }

    @SuppressWarnings("unchecked")
    public SimpleRepository(DB db, String tableName) {
        this(db, tableName, null);
    }

    @SuppressWarnings("unchecked")
    public SimpleRepository(DB db, String tableName, String tenant) {
        this.ds = new SimpleDataStore(new DBHandleProvider(db), db.getTablesPrefix());
        Type[] generics = ClassUtil.lookupGeneric(this.getClass(), SimpleRepository.class);
        Preconditions.checkNotNull(generics, "No EntityRepository found in class hierarchy");
        this.entityClass = (Class<E>) generics[0];
        this.tableName = tableName;
        if (TextUtil.isNotEmpty(tableName)) {
            EntityInfo.registerTable(entityClass, tableName);
        }
        if (TextUtil.isNotEmpty(tenant)) {
            lockedTenant = TenantId.of(tenant);
        }
    }

    public SimpleRepository(DB db, Class<E> entityClass) {
        this(db, entityClass, null);
    }

    public SimpleRepository(DB db, Class<E> entityClass, String tableName) {
        this(db, entityClass, tableName, null);
    }

    @Override
    public long count(TenantId tenant, Criteria criteria) {
        return ds.count(resolveTenant(tenant), entityClass, criteria);
    }


    @Override
    public int execute(TenantId tenant, String command) {
        return ds.execute(tenant, evaluateQuery(command));
    }

    @Override
    public long count(TenantId tenant) {
        return ds.count(resolveTenant(tenant), entityClass);
    }

    @Override
    public EntityRepository<E, I> withTenant(TenantId tenant) {
        return new SimpleRepository<>(this.ds, this.entityClass, this.tableName, resolveTenant(tenant).getValue());
    }

    @Override
    public PagedList<E> findAll(TenantId tenant, Paging paging) {
        return find(resolveTenant(tenant), null, paging);
    }

    @Override
    public Set<String> pluck(TenantId tenant, String field, int page, int count) {
        return ds.pluck(resolveTenant(tenant), entityClass, field, page, count);
    }

    @Override
    public void pluckStream(TenantId tenant, String field, int page, int count, Consumer<Stream<String>> consumer) {
        ds.pluckStream(resolveTenant(tenant), entityClass, field, page, count, consumer);
    }

    @Override
    public PagedList<E> find(TenantId tenant, Criteria criteria, Paging paging) {
        return ds.find(resolveTenant(tenant), entityClass, criteria, paging);
    }

    @Override
    public DataStore getDataStore() {
        return ds;
    }

    @Override
    public int truncate(TenantId tenant) {
        return ds.truncate(resolveTenant(tenant), entityClass);
    }

    @Override
    public <T> List<T> query(TenantId tenant, String query, Map<String, Object> binding, Class<T> resultClass) {
        return ds.query(resolveTenant(tenant), evaluateQuery(query), binding, resultClass);
    }

    @Override
    public double sumBy(TenantId tenant, String field, Criteria criteria) {
        return ds.sumBy(resolveTenant(tenant), entityClass, field, criteria);
    }

    @Override
    public Optional<E> get(TenantId tenant, Criteria criteria) {
        return ds.get(resolveTenant(tenant), entityClass, criteria);
    }

    @Override
    public Optional<E> findById(I id) {
        return ds.findById(resolveTenant(), entityClass, id);
    }

    @Override
    public Optional<E> findById(TenantId tenant, I id) {
        return ds.findById(resolveTenant(tenant), entityClass, id);
    }

    @Override
    public E insert(TenantId tenant, E entity) {
        return ds.insert(resolveTenant(tenant), entity);
    }

    @Override
    public int[] insert(TenantId tenant, List<E> entities) {
        return ds.insert(resolveTenant(tenant), entities);
    }

    @Override
    public E update(E entity, String... fields) {
        return ds.update(resolveTenant(), entity, fields);
    }

    @Override
    public String getTableName() {
        return ds.getTableName(tableName);
    }

    @Override
    public long loadCsvFile(TenantId tenant, File file, String delimiter) {
        return ds.loadCsvFile(resolveTenant(tenant), tableName, file, delimiter);
    }

    @Override
    public long loadCsvFile(TenantId tenant, InputStream input, String delimiter) {
        return ds.loadCsvFile(resolveTenant(tenant), tableName, input, delimiter);
    }

    @Override
    public long exportToCsvFile(TenantId tenant, String query, Map<String, Object> binding, File file, char delimiter, boolean headers) {
        return ds.exportToCsvFile(tenant, evaluateQuery(query), binding, file, delimiter, headers);
    }

    @Override
    public long exportToCsvFile(TenantId tenant, String query, Map<String, Object> binding, OutputStream out, char delimiter, boolean headers) {
        return ds.exportToCsvFile(tenant, evaluateQuery(query), binding, out, delimiter, headers);
    }

    @Override
    public E update(TenantId tenant, E entity, String... fields) {
        return ds.update(resolveTenant(tenant), entity, fields);
    }

    @Override
    public int delete(E entity) {
        return ds.delete(resolveTenant(), entity);
    }

    @Override
    public int delete(TenantId tenant, E entity) {
        return ds.delete(resolveTenant(tenant), entity);
    }

    @Override
    public int delete(Criteria criteria) {
        return ds.delete(resolveTenant(), entityClass, criteria);
    }

    @Override
    public void useTransaction(TenantId tenant, Consumer<EntityRepository<E, I>> consumer) {
        ds.useTransaction(resolveTenant(tenant), (ds) -> {
            SimpleRepository<E, I> ser = new SimpleRepository<>(ds, entityClass, tableName, lockedTenant.getValue());
            consumer.accept(ser);
        });
    }

    protected TenantId resolveTenant() {
        return resolveTenant(TenantId.CONTEXT);
    }


    @Override
    public TenantId resolveTenant(TenantId tenant) {
        if (!TenantId.CONTEXT.equals(lockedTenant)) {
            return lockedTenant;
        }
        if (TenantId.CONTEXT.equals(tenant)) {
            return TenantId.of(TenantHolder.require());
        }
        return tenant;
    }

    private String evaluateQuery(String query) {
        if (TextUtil.isNotEmpty(tableName)) {
            return query.replace(PH_TABLE, ds.getTableName(this.tableName));
        }
        return query;
    }

}
