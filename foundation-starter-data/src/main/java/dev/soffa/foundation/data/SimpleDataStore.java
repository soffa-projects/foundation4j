package dev.soffa.foundation.data;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.data.jdbi.BeanMapper;
import dev.soffa.foundation.data.jdbi.DBIHandleProvider;
import dev.soffa.foundation.data.jdbi.HandleHandleProvider;
import dev.soffa.foundation.data.jdbi.HandleProvider;
import dev.soffa.foundation.error.DatabaseException;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.helper.ID;
import dev.soffa.foundation.model.PagedList;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.GodClass")
public class SimpleDataStore implements DataStore {

    private static final String TABLE = "table";
    private static final String ORDER = "order";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String ID_COLUMN = "idColumn";
    private static final String ID_FIELD = "idField";
    private static final String WHERE = "where";
    private static final String VALUE = "value";
    private static final String BINDING = "binding";
    private static final String COLUMNS = "columns";
    private static final String VALUES = "values";

    // private DB db;
    // private Jdbi dbi;
    private final HandleProvider hp;
    private final String tablesPrefix;

    public SimpleDataStore(HandleProvider dataStoreHandle) {
        this(dataStoreHandle, "");
    }

    public SimpleDataStore(HandleProvider dataStoreHandle, String tablesPrefix) {
        this.hp = dataStoreHandle;
        this.tablesPrefix = TextUtil.trimToEmpty(tablesPrefix);
    }

    /*public SimpleDataStore(@NonNull DB db) {
            this.db = db;
            this.dataStoreHandle = new DataStoreHandle(this);
        }

         */
    public static SimpleDataStore create(@NonNull String dbUrl) {
        ExtDataSource props = ExtDataSource.create("default", "default", dbUrl);
        Jdbi dbi = Jdbi.create(props.getUrl(), props.getUsername(), props.getPassword());
        return new SimpleDataStore(new DBIHandleProvider(dbi));
    }

    @Override
    public <E> E insert(TenantId tenant, @NonNull E model) {
        prepare(model);
        return inTransaction(tenant, model.getClass(), (h, info) -> {
            h.createUpdate("INSERT INTO <table> (<columns>) VALUES (<values>)")
                .define(TABLE, info.getTableName())
                .defineList(COLUMNS, info.getColumnsEscaped())
                .defineList(VALUES, info.getValuesPlaceholder())
                .bindBean(model)
                .execute();
            return model;
        });
    }


    private <E> void prepare(@NonNull E model) {
        if (model instanceof EntityLifecycle) {
            EntityLifecycle lc = (EntityLifecycle) model;
            lc.onInsert();
            lc.onSave();
        }
        if (model instanceof EntityModel) {
            EntityModel em = (EntityModel) model;
            if (em.getCreated() == null) {
                em.setCreated(Date.from(Instant.now()));
            }
            if (TextUtil.isEmpty(em.getId())) {
                em.setId(ID.generate());
            }
        }
    }

    @Override
    public <E> int[] batch(TenantId tenant, @NonNull List<E> entities) {
        for (E model : entities) {
            prepare(model);
        }
        return inTransaction(tenant, entities.get(0).getClass(), (h, info) -> {
            // EL
            return h.prepareBatch("INSERT INTO <table> (<columns>) VALUES <values>")
                .define(TABLE, info.getTableName())
                .defineList(COLUMNS, info.getColumnsEscaped())
                //.defineList(VALUES, info.getValuesPlaceholder())
                .bindBeanList("values", entities, info.getColumns())
                .execute();
        });
    }

    @Override
    public <E> int[] batch(TenantId tenant, @NonNull String table, @NonNull List<E> entities) {
        for (E model : entities) {
            prepare(model);
        }
        return inTransaction(tenant, entities.get(0).getClass(), (h, info) -> {
            // EL
            return h.prepareBatch("INSERT INTO <table> (<columns>) VALUES <values>")
                .define(TABLE, table)
                .defineList(COLUMNS, info.getColumnsEscaped())
                .bindBeanList("values", entities, info.getColumns())
                .execute();
        });
    }


    @Override
    public <E> E update(TenantId tenant, @NonNull E model, String... fields) {

        if (model instanceof EntityLifecycle) {
            EntityLifecycle lc = (EntityLifecycle) model;
            lc.onUpdate();
            lc.onSave();
        }
        return inTransaction(tenant, model.getClass(), (h, info) -> {
            if (TextUtil.isEmpty(info.getIdProperty())) {
                throw new TechnicalException("No @Id field defined for Entity %s", model.getClass());
            }
            List<String> columns = info.getUpdatePairs();
            if (fields != null) {
                columns = Arrays.stream(fields).collect(Collectors.toList());
            }
            h.createUpdate("UPDATE <table> SET <columns> WHERE <idColumn> = :<idField>")
                .define(TABLE, info.getTableName())
                .defineList(COLUMNS, columns)
                .defineList(ID_COLUMN, info.getIdColumn())
                .defineList(ID_FIELD, info.getIdProperty())
                .bindBean(model)
                .execute();
            return model;
        });
    }

    @Override
    public <E> int delete(TenantId tenant, E model) {
        return inTransaction(tenant, model.getClass(), (handle, info) -> {
            // EL
            return handle.createUpdate("DELETE FROM <table> WHERE <idColumn> = :<idField>")
                .define(TABLE, info.getTableName())
                .defineList(ID_COLUMN, info.getIdColumn())
                .defineList(ID_FIELD, info.getIdProperty())
                .bindBean(model)
                .execute();
        });
    }

    @Override
    public int execute(TenantId tenant, String command) {
        return hp.inTransaction(tenant, (handle) -> handle.createUpdate(command).execute());
    }

    @Override
    public <E> int delete(TenantId tenant, @NonNull Class<E> entityClass, @NonNull Criteria criteria) {
        return inTransaction(tenant, entityClass, (handle, info) -> {
            // EL
            return handle.createUpdate("DELETE FROM <table> WHERE <where>")
                .define(TABLE, info.getTableName())
                .define(WHERE, criteria.getWhere())
                .bindMap(criteria.getBinding())
                .execute();
        });
    }

    @Override
    public <T> List<T> query(String query, Class<T> resultClass) {
        return hp.withHandle(null, handle -> handle.createQuery(query).map(BeanMapper.of(EntityInfo.of(resultClass))).list());
    }

    @Override
    public void useTransaction(TenantId tenant, Consumer<DataStore> consumer) {
        hp.inTransaction(tenant, (handle) -> {
            consumer.accept(new SimpleDataStore(new HandleHandleProvider(handle), tablesPrefix));
            return null;
        });
    }

    @Override
    public <E> PagedList<E> findAll(TenantId tenant, Class<E> entityClass, Paging paging) {
        return withHandle(tenant, entityClass, (handle, info) -> {
            // EL
            List<E> items = buildQuery(handle, entityClass, null, paging)
                .map(BeanMapper.of(info)).collect(Collectors.toList());
            long total = count(tenant, entityClass, null);
            return PagedList.of(items, total, paging);
        });
    }

    @Override
    public <E> PagedList<E> find(TenantId tenant, Class<E> entityClass, Criteria criteria, Paging paging) {
        return withHandle(tenant, entityClass, (handle, info) -> {
            //EL
            List<E> items = buildQuery(handle, entityClass, criteria, paging)
                .setMaxRows(paging.getSize())
                .map(BeanMapper.of(info)).collect(Collectors.toList());
            long total = count(tenant, entityClass, criteria);
            return PagedList.of(items, total, paging);
        });
    }

    @Override
    public <E> Optional<E> get(TenantId tenant, Class<E> entityClass, Criteria criteria) {
        return withHandle(tenant, entityClass, (handle, info) -> {
            //EL
            return buildQuery(handle, entityClass, criteria, null)
                .map(BeanMapper.of(info)).findFirst();
        });
    }

    @Override
    public boolean ping() {
        hp.withHandle(null, handle -> handle.createQuery("SELECT 1").mapToMap().findFirst());
        return true;
    }

    @Override
    public <E> Optional<E> findById(TenantId tenant, Class<E> entityClass,
                                    Object value) {
        return withHandle(tenant, entityClass, (handle, info) -> {
            //EL
            return handle.createQuery("SELECT * FROM <table> WHERE <idColumn> = :value")
                .define(TABLE, info.getTableName())
                .define(ID_COLUMN, info.getIdColumn())
                .bind(VALUE, value)
                .map(BeanMapper.of(info)).findFirst();
        });
    }

    @Override
    public <E> long count(TenantId tenant, @NonNull Class<E> entityClass) {
        return withHandle(tenant, entityClass, (handle, info) -> {
            //EL
            return handle.createQuery("SELECT COUNT(*) from <table>")
                .define(TABLE, info.getTableName())
                .mapTo(Long.class).first();
        });
    }

    @Override
    public <E> long count(TenantId tenant, @NonNull Class<E> entityClass, @Nullable Criteria criteria) {
        return withHandle(tenant, entityClass, (handle, info) -> {
            // EL
            return buildQuery(handle, entityClass, "SELECT COUNT(*)", criteria, null)
                .mapTo(Long.class).first();
        });
    }


    @Override
    public <E> double sumBy(TenantId tenant, @NonNull Class<E> entityClass, @NonNull String field, Criteria criteria) {
        return withHandle(tenant, entityClass, (handle, info) -> {
            return buildQuery(handle, entityClass, "SELECT SUM(<field>)", criteria, null)
                .define("field", field)
                .mapTo(Double.class).findFirst().orElse(0d);
        });
    }


    // =================================================================================================================

    private <E> Query buildQuery(Handle handle, Class<E> entityClass, @Nullable Criteria criteria, Paging paging) {
        return buildQuery(handle, entityClass, "SELECT *", criteria, paging);
    }

    private <E> Query buildQuery(Handle handle,
                                 Class<E> entityClass,
                                 String baseQuery,
                                 @Nullable Criteria criteria,
                                 @Nullable Paging paging) {
        EntityInfo<E> info = EntityInfo.of(entityClass, tablesPrefix);
        Query q = define(
            info,
            paging,
            handle.createQuery(createBaseQuery(baseQuery, paging != null, criteria != null))
        );
        if (criteria != null) {
            q.define(WHERE, criteria.getWhere())
                .defineList(BINDING, criteria.getBinding())
                .bindMap(criteria.getBinding());
        }
        return q;
    }

    private String createBaseQuery(String base, boolean paging, boolean criteria) {
        StringBuilder sb = new StringBuilder(base).append(" FROM <table>");
        if (criteria) {
            sb.append(" WHERE <where>");
        }
        if (paging) {
            sb.append(" ORDER BY <order> LIMIT <limit> OFFSET <offset>");
        }
        return sb.toString();
    }

    private Query define(EntityInfo<?> info, Paging paging, Query query) {
        if (paging == null) {
            return query.define(TABLE, info.getTableName());
        }
        Paging p = Paging.of(paging);
        if ("id".equalsIgnoreCase(p.getSort())) {
            p.setSort(info.getIdColumn());
        }
        return query.define(TABLE, info.getTableName())
            .define(ORDER, p.getSort())
            .define(LIMIT, p.getSize())
            .define(OFFSET, (p.getPage() - 1) * p.getSize());
    }

    private <T, E> T inTransaction(TenantId tenant,
                                   Class<E> entityClass,
                                   BiFunction<Handle, EntityInfo<E>, T> consumer) {
        return TenantHolder.use(tenant, () -> {
            try {
                EntityInfo<E> info = EntityInfo.of(entityClass, tablesPrefix);
                return hp.inTransaction(tenant, handle -> consumer.apply(handle, info));
            } catch (Exception e) {
                Logger.platform.error("Current tenant is: %s", tenant);
                throw new DatabaseException(e);
            }
        });
    }


    private <T, E> T withHandle(TenantId tenant,
                                Class<E> entityClass,
                                BiFunction<Handle, EntityInfo<E>, T> consumer) {
        return TenantHolder.use(tenant, () -> {
            try {
                EntityInfo<E> info = EntityInfo.of(entityClass, tablesPrefix);
                return hp.withHandle(tenant, handle -> consumer.apply(handle, info));
            } catch (Exception e) {
                Logger.platform.error("Current tenant is: %s", tenant);
                throw new DatabaseException(e);
            }
        });
    }


}
