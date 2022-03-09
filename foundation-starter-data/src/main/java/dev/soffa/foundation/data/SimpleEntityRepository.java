package dev.soffa.foundation.data;

import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.models.TenantId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleEntityRepository<E> implements EntityRepository<E> {

    private final DataStore ds;
    private final Class<E> entityClass;

    public SimpleEntityRepository(DataStore ds, Class<E> entityClass) {
        this(ds, entityClass, null);
    }

    public SimpleEntityRepository(DataStore ds, Class<E> entityClass, String tableName) {
        this.ds = ds;
        this.entityClass = entityClass;
        if (TextUtil.isNotEmpty(tableName)) {
            EntityInfo.registerTable(entityClass, tableName);
        }
    }

    public SimpleEntityRepository(DB db, Class<E> entityClass) {
        this(new SimpleDataStore(db), entityClass, null);
    }

    public SimpleEntityRepository(DB db, Class<E> entityClass, String tableName) {
        this(new SimpleDataStore(db), entityClass, tableName);
    }

    @Override
    public long count() {
        return ds.count(getLockedTenant(), entityClass);
    }

    @Override
    public long count(String where, Map<String, Object> binding) {
        return ds.count(getLockedTenant(), entityClass, where, binding);
    }

    @Override
    public List<E> findAll() {
        return ds.findAll(getLockedTenant(), entityClass);
    }

    @Override
    public List<E> find(String where, Map<String, Object> binding) {
        return ds.find(getLockedTenant(), entityClass, where, binding);
    }

    @Override
    public Optional<E> get(String where, Map<String, Object> binding) {
        return ds.get(getLockedTenant(), entityClass, where, binding);
    }

    @Override
    public Optional<E> findById(Object id) {
        return ds.findById(getLockedTenant(), entityClass, id);
    }

    @Override
    public E insert(E entity) {
        return ds.insert(getLockedTenant(), entity);
    }

    @Override
    public E update(E entity) {
        return ds.update(getLockedTenant(), entity);
    }

    @Override
    public int delete(E entity) {
        return ds.delete(getLockedTenant(), entity);
    }

    @Override
    public int delete(String where, Map<String, Object> binding) {
        return ds.delete(getLockedTenant(), entityClass, where, binding);
    }

    protected TenantId getLockedTenant() {
        return TenantId.INHERIT;
    }


}
