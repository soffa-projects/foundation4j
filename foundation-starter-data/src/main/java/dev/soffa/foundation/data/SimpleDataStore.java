package dev.soffa.foundation.data;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.data.jdbi.*;
import dev.soffa.foundation.error.DatabaseException;
import dev.soffa.foundation.error.ErrorUtil;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.helper.ID;
import dev.soffa.foundation.model.PagedList;
import dev.soffa.foundation.model.Paging;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.*;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("PMD.GodClass")
public class SimpleDataStore implements DataStore {

    public static final int COPY_BUFFER_SIZE = 65_536 * 4;
    private static final String TABLE = "table";
    private static final String ORDER = "order";
    private static final String LIMIT = "limit";
    private static final String FIELD = "field";
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

    public SimpleDataStore(@NonNull String dbUrl) {
        ExtDataSource props = ExtDataSource.create("default", "default", dbUrl);
        Jdbi dbi;
        String username = props.getUsername();
        String password = props.getPassword();
        if (TextUtil.isNotEmpty(username) && TextUtil.isNotEmpty(password)) {
            dbi = Jdbi.create(props.getUrl(), username, password);
        }else {
             dbi = Jdbi.create(props.getUrl());
        }
        boolean isPG = dbUrl.startsWith("pg://") || dbUrl.startsWith("postgres");
        JdbiUtil.configure(dbi, isPG);
        this.tablesPrefix = "";
        this.hp = new DBIHandleProvider(dbi);
    }
    public static SimpleDataStore create(@NonNull String dbUrl) {
       return new SimpleDataStore(dbUrl);
    }

    @Override
    public String getTablesPrefix() {
        return tablesPrefix;
    }

    @Override
    public <E> int[] insert(TenantId tenant, @NonNull List<E> entities) {
        return batch(tenant, entities);
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

    @Override
    public <E> int truncate(TenantId tenant, Class<E> entityClass) {
        return inTransaction(tenant, entityClass, (h, info) -> {
            // EL
            return h.createUpdate("TRUNCATE TABLE <table>")
                .define(TABLE, info.getTableName())
                .execute();
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
        if (entities.isEmpty()) {
            return new int[0];
        }
        for (E model : entities) {
            prepare(model);
        }
        return inTransaction(tenant, entities.get(0).getClass(), (h, info) -> {
            // EL
            PreparedBatch b = h.prepareBatch("INSERT INTO <table> (<columns>) VALUES (<values>)")
                .define(TABLE, info.getTableName())
                .defineList(COLUMNS, info.getColumnsEscaped())
                .define(VALUES, String.join(",", info.getValuesPlaceholder()));
            for (E entity : entities) {
                b.bindBean(entity).add();
            }
            return b.execute();
        });
    }

    @Override
    public <E> int[] batch(TenantId tenant, @NonNull String table, @NonNull List<E> entities) {
        if (entities.isEmpty()) {
            return new int[0];
        }
        for (E model : entities) {
            prepare(model);
        }
        return inTransaction(tenant, entities.get(0).getClass(), (h, info) -> {
            // EL
            PreparedBatch b = h.prepareBatch("INSERT INTO <table> (<columns>) VALUES <values>")
                .define(TABLE, table)
                .defineList(COLUMNS, info.getColumnsEscaped())
                .define(VALUES, String.join(",", info.getValuesPlaceholder()));

            for (E entity : entities) {
                b.bindBean(entity).add();
            }
            return b.execute();
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
            List<String> columns = info.getUpdatePairs(fields);
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
    public <E> int[] updateBatch(TenantId tenant, @NonNull List<E> models, String... fields) {

        if (models.isEmpty()) {
            return new int[0];
        }

        for (E model : models) {
            if (model instanceof EntityLifecycle) {
                EntityLifecycle lc = (EntityLifecycle) model;
                lc.onUpdate();
                lc.onSave();
            }
        }

        return inTransaction(tenant, models.get(0).getClass(), (h, info) -> {
            if (TextUtil.isEmpty(info.getIdProperty())) {
                throw new TechnicalException("No @Id field defined for Entity %s", models.get(0).getClass());
            }

            Batch batch = h.createBatch();
            DbSpec spec = new DbSpec();
            DbSchema schema = spec.addDefaultSchema();
            DbTable table = new DbTable(schema, info.getTableName());
            List<String> columns = info.getUpdatePairs(fields);
            Map<DbColumn,String> setFields = new HashMap<>();
            DbColumn idColumn = table.addColumn(info.getIdColumn());
            for (String column : columns) {
                String[] parts = column.trim().split("=");
                String field = parts[1].trim().replace(":", "");
                setFields.put(table.addColumn(parts[0].trim()), field);
            }

            for (E model : models) {
                UpdateQuery query = new UpdateQuery(info.getTableName());
                try {
                    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") BeanMap beanMap = new BeanMap(model);
                    Object idValue = beanMap.get(info.getIdProperty());
                    query.addCondition(BinaryCondition.equalTo(idColumn, idValue));
                    for (Map.Entry<DbColumn, String> e : setFields.entrySet()) {
                        Object value = beanMap.get(e.getValue());
                        query.addSetClause(e.getKey(), value);
                    }
                    String sql = query.toString();
                    batch.add(sql);
                } catch (Exception e) {
                    throw new TechnicalException(e);
                }
            }

            return batch.execute();
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
        if (command.toUpperCase().startsWith("VACUUM")) {
            return hp.withHandle(tenant, (handle) -> handle.createUpdate(command).execute());
        } else {
            return hp.inTransaction(tenant, (handle) -> handle.createUpdate(command).execute());
        }

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
    public <T> List<T> query(TenantId tenant, String query, Map<String, Object> binding, Class<T> resultClass) {
        return hp.withHandle(tenant, handle -> {
            Query q = handle.createQuery(query);
            if (binding != null && !binding.isEmpty()) {
                q.bindMap(binding);
            }
            return q.map(BeanMapper.of(EntityInfo.of(resultClass, null, false))).list();
        });
    }
    @Override
    public <T>  T queryOne(TenantId tenant, String query, Map<String, Object> binding, Class<T> resultClass) {
        return hp.withHandle(tenant, handle -> {
            Query q = handle.createQuery(query);
            if (binding != null && !binding.isEmpty()) {
                q.bindMap(binding);
            }
            return q.map(BeanMapper.of(EntityInfo.of(resultClass, null, false))).one();
        });
    }

    @Override
    public <T> void withStream(TenantId tenant, String query, Map<String, Object> binding, Class<T> resultClass, Consumer<Stream<T>> handler) {
        hp.useHandle(tenant, handle -> {
            Query q = handle.createQuery(query);
            if (binding != null && !binding.isEmpty()) {
                q.bindMap(binding);
            }
            q.map(BeanMapper.of(EntityInfo.of(resultClass, null, false))).withStream(stream -> {
                handler.accept(stream);
                return null;
            });
        });
    }

    @Override
    public void useTransaction(TenantId tenant, Consumer<DataStore> consumer) {
        hp.inTransaction(tenant, (handle) -> {
            consumer.accept(new SimpleDataStore(new HandleHandleProvider(handle), tablesPrefix));
            return null;
        });
    }

    @Override
    public <T> Set<String> pluck(TenantId tenant, Class<T> entityClass, String field, int page, int count) {
        return withHandle(tenant, entityClass, (h, info) -> {
            // EL
            List<String> data;
            if (count > 0 && count < Integer.MAX_VALUE) {
                data = h.createQuery("SELECT distinct <field> from <table> ORDER BY <field> LIMIT <limit> OFFSET <offset>")
                    .define(TABLE, info.getTableName())
                    .define(FIELD, field)
                    .define(LIMIT, count)
                    .define(OFFSET, (page - 1) * count)
                    .mapTo(String.class).list();
            } else {
                data = h.createQuery("SELECT distinct <field> from <table>")
                    .define(TABLE, info.getTableName())
                    .define(FIELD, field)
                    .mapTo(String.class).list();
            }
            return new HashSet<>(data);
        });
    }

    @Override
    public <T> void pluckStream(TenantId tenant, Class<T> entityClass, String field, int page, int count, Consumer<Stream<String>> consumer) {
        withHandle(tenant, entityClass, (h, info) -> {
            // EL
            Stream<String> stream;
            if (count > 0 && count < Integer.MAX_VALUE) {
                stream = h.createQuery("SELECT distinct <field> from <table> ORDER BY <field> LIMIT <limit> OFFSET <offset>")
                    .define(TABLE, info.getTableName())
                    .define(FIELD, field)
                    .define(LIMIT, count)
                    .define(OFFSET, (page - 1) * count)
                    .mapTo(String.class).stream();
            } else {
                stream = h.createQuery("SELECT distinct <field> from <table>")
                    .define(TABLE, info.getTableName())
                    .define(FIELD, field)
                    .mapTo(String.class).stream();
            }
            consumer.accept(stream);
            return null;
        });
    }

    @Override
    public long loadCsvFile(TenantId tenant, String tableName, File file, String delimiter) {
        return hp.inTransaction(tenant, handle -> {
            try {
                CopyManager cm = new CopyManager(handle.getConnection().unwrap(BaseConnection.class));
                String sql = String.format("COPY %s FROM STDIN ( DELIMITER '%s'  )", tablesPrefix + tableName, delimiter);
                return cm.copyIn(sql, Files.newBufferedReader(file.toPath()), COPY_BUFFER_SIZE);
            } catch (SQLException | IOException e) {
                throw new DatabaseException(e);
            }
        });
    }

    @Override
    public long loadCsvFile(TenantId tenant, String tableName, InputStream input, String delimiter) {
        return hp.inTransaction(tenant, handle -> {
            try {
                CopyManager cm = new CopyManager(handle.getConnection().unwrap(BaseConnection.class));
                String sql = String.format("COPY %s FROM STDIN ( DELIMITER '%s'  )", tablesPrefix + tableName, delimiter);
                return cm.copyIn(sql, input, COPY_BUFFER_SIZE);
            } catch (SQLException | IOException e) {
                throw new DatabaseException(e);
            }
        });
    }


    @SneakyThrows
    @Override
    public long exportToCsvFile(TenantId tenant, String query, Map<String, Object> binding, File file, char delimiter, boolean headers) {
        try (OutputStream writer = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            return exportToCsvFile(tenant, query, binding, writer, delimiter, headers);
        }
    }

    @Override
    public long exportToCsvFile(TenantId tenant, String query, Map<String, Object> binding, OutputStream out, char delimiter, boolean headers) {
        return hp.withHandle(tenant, handle -> {
            try {
                CopyManager cm = new CopyManager(handle.getConnection().unwrap(BaseConnection.class));
                String sql = String.format(
                    "COPY (%s) TO STDOUT DELIMITER '%s' %s",
                    query,
                    delimiter,
                    headers ? "CSV HEADER" : ""
                );
                return cm.copyOut(sql, out);
            } catch (Exception e) {
                throw new DatabaseException(e);
            } finally {
                try {
                    out.flush();
                } catch (IOException e) {
                    Logger.platform.warn("Error while flushing output stream: %s", ErrorUtil.getError(e));
                }
            }
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
            handle.createQuery(createBaseQuery(baseQuery, paging, criteria != null))
        );
        if (criteria != null) {
            q.define(WHERE, criteria.getWhere())
                .defineList(BINDING, criteria.getBinding())
                .bindMap(criteria.getBinding());
        }
        return q;
    }

    private String createBaseQuery(String base, Paging paging, boolean criteria) {
        StringBuilder sb = new StringBuilder(base).append(" FROM <table>");
        if (criteria) {
            sb.append(" WHERE <where>");
        }
        if (paging != null && TextUtil.isNotEmpty(paging.getSort())) {
            sb.append(" ORDER BY <order>");
        }
        if (paging != null && paging.getSize() > 0) {
            sb.append(" LIMIT <limit> OFFSET <offset>");
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
                throw new DatabaseException(e);
            }
        });
    }


}
