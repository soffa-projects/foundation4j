package dev.soffa.foundation.data;

import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.model.TenantId;

import java.util.List;
import java.util.Optional;

public class SimpleEntityRepository<E> implements EntityRepository<E> {

    private final DataStore ds;
    private final Class<E> entityClass;
    private TenantId lockedTenant = TenantId.CONTEXT;

    public SimpleEntityRepository(DataStore ds, Class<E> entityClass) {
        this(ds, entityClass, null);
    }

    public SimpleEntityRepository(DataStore ds, Class<E> entityClass, String tableName) {
        this(ds, entityClass, tableName, null);
    }

    public SimpleEntityRepository(DataStore ds, Class<E> entityClass, String tableName, String tenant) {
        this.ds = ds;
        this.entityClass = entityClass;
        if (TextUtil.isNotEmpty(tableName)) {
            EntityInfo.registerTable(entityClass, tableName);
        }
        if (TextUtil.isNotEmpty(tenant)) {
            lockedTenant = TenantId.of(tenant);
        }
    }

    public SimpleEntityRepository(DB db, Class<E> entityClass) {
        this(new SimpleDataStore(db), entityClass, null);
    }

    public SimpleEntityRepository(DB db, Class<E> entityClass, String tableName) {
        this(new SimpleDataStore(db), entityClass, tableName);
    }

    @Override
    public long count(Criteria criteria) {
        return ds.count(getLockedTenant(), entityClass, criteria);
    }

    @Override
    public long count() {
        return ds.count(getLockedTenant(), entityClass);
    }

    @Override
    public List<E> findAll() {
        return find((Criteria) null);
    }

    @Override
    public List<E> find(Criteria criteria) {
        return ds.find(getLockedTenant(), entityClass, criteria);
    }

    @Override
    public Optional<E> get(Criteria criteria) {
        return ds.get(getLockedTenant(), entityClass, criteria);
    }

    @Override
    public Optional<E> get(TenantId tenant, Criteria criteria) {
        return ds.get(tenant, entityClass, criteria);
    }


    @Override
    public Optional<E> findById(Object id) {
        return ds.findById(getLockedTenant(), entityClass, id);
    }

    @Override
    public Optional<E> findById(TenantId tenant, Object value) {
        return ds.findById(tenant, entityClass, value);
    }

    @Override
    public E insert(E entity) {
        return ds.insert(getLockedTenant(), entity);
    }

    @Override
    public E insert(TenantId tenant, E entity) {
        return ds.insert(tenant, entity);
    }

    @Override
    public E update(E entity) {
        return ds.update(getLockedTenant(), entity);
    }

    @Override
    public E update(TenantId tenant, E entity) {
        return ds.update(tenant, entity);
    }

    @Override
    public int delete(E entity) {
        return ds.delete(getLockedTenant(), entity);
    }

    @Override
    public int delete(TenantId tenant, E entity) {
        return ds.delete(tenant, entity);
    }

    @Override
    public int delete(Criteria criteria) {
        return ds.delete(getLockedTenant(), entityClass, criteria);
    }

    protected TenantId getLockedTenant() {
        return lockedTenant;
    }


}
