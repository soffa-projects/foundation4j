package dev.soffa.foundation.data;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.jdbi.DBHandleProvider;
import dev.soffa.foundation.model.PagedList;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class SimpleRepository<E, I> implements EntityRepository<E, I> {

    private final DataStore ds;
    private final Class<E> entityClass;
    private TenantId lockedTenant = TenantId.CONTEXT;

    private final String tableName;

    public SimpleRepository(DataStore ds, Class<E> entityClass) {
        this(ds, entityClass, null);
    }

    public SimpleRepository(DataStore ds, Class<E> entityClass, String tableName) {
        this(ds, entityClass, tableName, null);
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
    public Set<String> pluck(TenantId tenant, String field) {
        return ds.pluck(resolveTenant(tenant), entityClass, field);
    }

    @Override
    public PagedList<E> find(TenantId tenant, Criteria criteria, Paging paging) {
        return ds.find(resolveTenant(tenant), entityClass, criteria, paging);
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
    public int loadCsvFile(TenantId tenant, String file, String delimiter) {
        return ds.loadCsvFile(resolveTenant(tenant), tableName, file, delimiter);
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


}
