package dev.soffa.foundation.data.spring;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.soffa.foundation.commons.DigestUtil;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Properties;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.common.HikariDS;
import dev.soffa.foundation.data.config.DataSourceProperties;
import dev.soffa.foundation.error.DatabaseException;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.model.TenantId;
import liquibase.integration.spring.SpringLiquibase;
import lombok.SneakyThrows;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.jdbi.v3.core.Jdbi;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DBHelper {

    private static final Logger LOG = Logger.get(DBHelper.class);
    private static final ResourceLoader RL = new DefaultResourceLoader();

    private static final Map<String, HikariDataSource> CACHE = new ConcurrentHashMap<>();


    private DBHelper() {
    }

    @SneakyThrows
    public static DataSource createDataSource(String sname, DataSourceProperties config) {
        String name = sname;
        if (TextUtil.isEmpty(name)) {
            name = config.getName();
        }

        String baseJdbcUrl = config.getUrl().split("\\?")[0];
        String cacheId = DigestUtil.md5(baseJdbcUrl);
        boolean isH2 = config.getUrl().contains(":h2:");

        if (CACHE.containsKey(cacheId)) {
            createSchema(CACHE.get(cacheId), config.getSchema());
            return new HikariDS(CACHE.get(cacheId), config.getSchema());
        }

        HikariConfig hc = new HikariConfig();

        hc.setDriverClassName(config.getDriverClassName());
        hc.setUsername(config.getUsername());
        hc.setPassword(config.getPassword());
        hc.setJdbcUrl(config.getUrl());
        hc.setPoolName(name);
        hc.setConnectionTestQuery("select 1");

        Properties props = new Properties(config.getProperties());

        hc.setMinimumIdle(props.getInt("minimumIdle", 0));
        hc.setConnectionTimeout(props.getInt("connectionTimeout", 30_000));
        hc.setIdleTimeout(props.getInt("idleTimeout", 30_000));
        hc.setMaxLifetime(props.getInt("maxLifetime", 45_000));
        hc.setMaximumPoolSize(props.getInt("maxPoolSize", 16));

        // hc.setLeakDetectionThreshold(10 * 1000);

        LOG.debug("Using jdbcUrl: %s", config.getUrl());

        if (isH2) {
            hc.addDataSourceProperty("ignore_startup_parameters", "search_path");
        }

        hc.addDataSourceProperty("autoReconnect", true);
        hc.addDataSourceProperty("cachePrepStmts", true);
        hc.addDataSourceProperty("prepStmtCacheSize", 250);
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hc.addDataSourceProperty("useServerPrepStmts", true);
        hc.addDataSourceProperty("cacheResultSetMetadata", true);


        if (config.hasSchema()) {
            hc.setSchema("@@$$auto$$@@");
        }

        HikariDataSource ds = new HikariDataSource(hc);

        if (!isH2 && !config.isDefaultSource()) {
            CACHE.put(cacheId, ds);
        }
        createSchema(ds, config.getSchema());
        return new HikariDS(ds, config.getSchema());
    }

    private static void createSchema(DataSource ds, String schema) {
        if (TextUtil.isEmpty(schema)) {
            return;
        }
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.execute("create schema if not exists " + schema);
    }

    public static void applyMigrations(DatasourceInfo dsInfo, String changeLogPath, String tablesPrefix, String appicationName) {
        SpringLiquibase lqb = new SpringLiquibase();
        lqb.setDropFirst(false);
        lqb.setResourceLoader(RL);
        Map<String, String> changeLogParams = new HashMap<>();

        changeLogParams.put("prefix", "");
        changeLogParams.put("table_prefix", "");
        changeLogParams.put("tables_prefix", "");
        changeLogParams.put("tablePrefix", "");
        changeLogParams.put("tablesPrefix", "");


        if (TextUtil.isNotEmpty(tablesPrefix)) {
            changeLogParams.put("prefix", tablesPrefix);
            changeLogParams.put("table_prefix", tablesPrefix);
            changeLogParams.put("tables_prefix", tablesPrefix);
            changeLogParams.put("tablePrefix", tablesPrefix);
            changeLogParams.put("tablesPrefix", tablesPrefix);

            lqb.setDatabaseChangeLogLockTable(tablesPrefix + "changelog_lock");
            lqb.setDatabaseChangeLogTable(tablesPrefix + "changelog");
        }
        if (TextUtil.isNotEmpty(appicationName)) {
            changeLogParams.put("application", appicationName);
            changeLogParams.put("applicationName", appicationName);
            changeLogParams.put("application_name", appicationName);
        }

        Resource res = RL.getResource(changeLogPath);
        if (!res.exists()) {
            throw new TechnicalException("Liquibase changeLog was not found: %s", changeLogPath);
        }
        lqb.setChangeLog(changeLogPath);
        doApplyMigration(dsInfo, lqb, changeLogParams);
    }

    @SuppressWarnings("resource")
    private static void doApplyMigration(DatasourceInfo dsInfo, SpringLiquibase lqb, Map<String, String> changeLogParams) {
        @SuppressWarnings("PMD.CloseResource")
        DataSource ds = dsInfo.getDataSource();
        String schema = null;
        if (ds instanceof HikariDataSource) {
            schema = ((HikariDataSource) ds).getSchema();
        } else if (ds instanceof HikariDS) {
            schema = ((HikariDS) ds).getSchema();
        }
        if (TenantId.DEFAULT_VALUE.equals(dsInfo.getName())) {
            lqb.setContexts(TenantId.DEFAULT_VALUE);
        } else {
            lqb.setContexts("tenant," + dsInfo.getName());
        }
        if (TextUtil.isNotEmpty(schema)) {
            lqb.setDefaultSchema(schema);
            lqb.setLiquibaseSchema(schema);
        }
        lqb.setChangeLogParameters(changeLogParams);
        try {
            lqb.setDataSource(ds);
            lqb.afterPropertiesSet(); // Run migrations
            LOG.info("[datasource:%s] migration '%s' successfully applied", dsInfo.getName(), lqb.getChangeLog());
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("changelog") && msg.contains("already exists")) {
                boolean isTestDb = unwrapDataSource(lqb.getDataSource()).getJdbcUrl().startsWith("jdbc:h2:mem");
                if (!isTestDb) {
                    LOG.warn("Looks like migrations are being ran twice for %s.%s, ignore this error", dsInfo.getName(), schema);
                }
            } else {
                throw new DatabaseException(e, "Migration failed for %s", schema);
            }
        }
    }

    private static HikariDataSource unwrapDataSource(DataSource source) {
        if (source instanceof HikariDataSource) {
            return (HikariDataSource) source;
        } else if (source instanceof HikariDS) {
            return ((HikariDS) source).unwrap();
        }
        throw new IllegalArgumentException("DataSource is not a HikariDataSource");
    }

    public static String findChangeLogPath(String applicationName, String migrationName) {
        String changelogPath = null;
        boolean hasMigration = !("false".equals(migrationName) || "no".equals(migrationName));
        if (hasMigration) {
            if (TextUtil.isNotEmpty(migrationName) && !"true".equals(migrationName)) {
                changelogPath = "/db/changelog/" + migrationName + ".xml";
            } else {
                changelogPath = "/db/changelog/" + applicationName + ".xml";
            }
            if (TextUtil.isNotEmpty(changelogPath)) {
                ResourceLoader resourceLoader = new DefaultResourceLoader();
                if (!resourceLoader.getResource(changelogPath).exists()) {
                    throw new TechnicalException("Changelog file not found: " + changelogPath);
                }
            }
        }
        return changelogPath;
    }


    @SneakyThrows
    public static LockProvider createLockTable(DataSource ds, String tablePrefix) {

        LockProvider lockProvider = new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(ds))
            .withTableName(tablePrefix + "f_shedlock")
            .withTimeZone(TimeZone.getTimeZone("UTC"))
            .build());

        try {
            Jdbi.create(ds).useTransaction(handle -> {
                // EL
                handle.createUpdate("CREATE TABLE IF NOT EXISTS <table>(name VARCHAR(64) NOT NULL, lock_until TIMESTAMP NOT NULL, locked_at TIMESTAMP NOT NULL, locked_by VARCHAR(255) NOT NULL, PRIMARY KEY (name))")
                    .define("table", tablePrefix + "f_shedlock")
                    .execute();
            });
        } catch (Exception e) {
            LOG.warn(e.getMessage());
            throw new DatabaseException(e);
        }
        return lockProvider;
    }

    @SneakyThrows
    public static void createPendingJobTable(DataSource ds, String tablePrefix) {
        try {
            Jdbi.create(ds).useTransaction(handle -> {
                // EL
                List<String> commands = new ArrayList<>();
                commands.add("CREATE TABLE IF NOT EXISTS <table>(" +
                    "id VARCHAR NOT NULL," +
                    "operation VARCHAR NOT NULL," +
                    "subject VARCHAR NOT NULL," +
                    "data TEXT," +
                    "metas TEXT," +
                    "last_error TEXT," +
                    "errors_count TEXT," +
                    "created TIMESTAMP NOT NULL," +
                    "PRIMARY KEY (id))");
                commands.add("CREATE INDEX IF NOT EXISTS <table>__created__idx ON <table>(created)");
                commands.add("CREATE INDEX IF NOT EXISTS <table>__subject__idx ON <table>(subject)");
                commands.add("CREATE INDEX IF NOT EXISTS <table>__operation__idx ON <table>(operation)");
                for (String command : commands) {
                    handle.createUpdate(command)
                        .define("table", TextUtil.trimToEmpty(tablePrefix) + "f_pending_jobs")
                        .execute();
                }
            });
        } catch (Exception e) {
            LOG.warn(e.getMessage());
            throw new DatabaseException(e);
        }
    }

}
