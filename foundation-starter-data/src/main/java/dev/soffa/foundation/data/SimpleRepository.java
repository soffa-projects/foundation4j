package dev.soffa.foundation.data;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.model.TenantId;

import java.util.List;
import java.util.Optional;

public class SimpleRepository<E, I> implements EntityRepository<E, I> {

    private final DataStore ds;
    private final Class<E> entityClass;
    private TenantId lockedTenant = TenantId.CONTEXT;

    public SimpleRepository(DataStore ds, Class<E> entityClass) {
        this(ds, entityClass, null);
    }

    public SimpleRepository(DataStore ds, Class<E> entityClass, String tableName) {
        this(ds, entityClass, tableName, null);
    }

    public SimpleRepository(DataStore ds, Class<E> entityClass, String tableName, String tenant) {
        this.ds = ds;
        this.entityClass = entityClass;
        if (TextUtil.isNotEmpty(tableName)) {
            EntityInfo.registerTable(entityClass, tableName);
        }
        if (TextUtil.isNotEmpty(tenant)) {
            lockedTenant = TenantId.of(tenant);
        }
    }
    public SimpleRepository(DB db, Class<E> entityClass, String tableName, String tenant) {
        this(new SimpleDataStore(db), entityClass, tenant);
    }

    @SuppressWarnings("unchecked")
    public SimpleRepository(DB db, String tableName) {
        this(db, tableName, null);
    }

    @SuppressWarnings("unchecked")
    public SimpleRepository(DB db, String tableName, String tenant) {
        this.ds = new SimpleDataStore(db);
        Class<?>[] generics = ClassUtil.lookupGeneric(this.getClass(), SimpleRepository.class);
        Preconditions.checkNotNull(generics, "No EntityRepository found in class hierarchy");
        this.entityClass = (Class<E>)generics[0];
        if (TextUtil.isNotEmpty(tableName)) {
            EntityInfo.registerTable(entityClass, tableName);
        }
        if (TextUtil.isNotEmpty(tenant)) {
            lockedTenant = TenantId.of(tenant);
        }
    }

    public SimpleRepository(DB db, Class<E> entityClass) {
        this(new SimpleDataStore(db), entityClass, null);
    }

    public SimpleRepository(DB db, Class<E> entityClass, String tableName) {
        this(new SimpleDataStore(db), entityClass, tableName);
    }

    @Override
    public long count(TenantId tenant, Criteria criteria) {
        return ds.count(resolveTenant(tenant), entityClass, criteria);
    }

    @Override
    public long count() {
        return ds.count(resolveTenant(), entityClass);
    }

    @Override
    public List<E> findAll(TenantId tenant) {
        return find(resolveTenant(tenant), null);
    }

    @Override
    public List<E> find(TenantId tenant, Criteria criteria) {
        return ds.find(resolveTenant(tenant), entityClass, criteria);
    }

    @Override
    public Optional<E> get(Criteria criteria) {
        return ds.get(resolveTenant(), entityClass, criteria);
    }

    @Override
    public Optional<E> get(TenantId tenant, Criteria criteria) {
        return ds.get(tenant, entityClass, criteria);
    }

    @Override
    public Optional<E> findById(I id) {
        return ds.findById(resolveTenant(), entityClass, id);
    }

    @Override
    public Optional<E> findById(TenantId tenant, I id) {
        return ds.findById(tenant, entityClass, id);
    }

    @Override
    public E insert(E entity) {
        return ds.insert(resolveTenant(), entity);
    }

    @Override
    public E insert(TenantId tenant, E entity) {
        return ds.insert(tenant, entity);
    }

    @Override
    public E update(E entity) {
        return ds.update(resolveTenant(), entity);
    }

    @Override
    public E update(TenantId tenant, E entity) {
        return ds.update(tenant, entity);
    }

    @Override
    public int delete(E entity) {
        return ds.delete(resolveTenant(), entity);
    }

    @Override
    public int delete(TenantId tenant, E entity) {
        return ds.delete(tenant, entity);
    }

    @Override
    public int delete(Criteria criteria) {
        return ds.delete(resolveTenant(), entityClass, criteria);
    }

    protected TenantId resolveTenant() {
        return lockedTenant;
    }

    protected TenantId resolveTenant(TenantId tenant) {
        if (tenant != null && !tenant.equals(TenantId.CONTEXT)) {
            return tenant;
        }
        return lockedTenant;
    }


}
