package dev.soffa.foundation.data.spring;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.data.DataSourceConfig;
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
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public final class DBHelper {

    private static final Logger LOG = Logger.get(DBHelper.class);
    private static final ResourceLoader RL = new DefaultResourceLoader();

    private static final Map<String, HikariDataSource> CACHE = new ConcurrentHashMap<>();


    private DBHelper() {
    }

    @SneakyThrows
    public static DataSource createDataSource(String name, DataSourceProperties config) {

        String baseJdbcUrl = config.getUrl().split("\\?")[0];
        String cacheId = DigestUtil.md5(baseJdbcUrl);

        if (CACHE.containsKey(cacheId)) {
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

        hc.setMinimumIdle(props.getInt("minimumIdle", 10_000));
        hc.setConnectionTimeout(props.getInt("connectionTimeout", 30_000));
        hc.setIdleTimeout(props.getInt("idleTimeout", 35_000));
        hc.setMaxLifetime(props.getInt("maxLifetime", 45_000));
        hc.setMaximumPoolSize(props.getInt("maxPoolSize", 10));

        LOG.debug("Using jdbcUrl: %s", config.getUrl());

        if (config.getUrl().contains(":h2:")) {
            hc.addDataSourceProperty("ignore_startup_parameters", "search_path");
        }

        if (config.hasSchema()) {
            hc.setSchema("public");
        }

        CACHE.put(cacheId, new HikariDataSource(hc));
        return new HikariDS(CACHE.get(cacheId), config.getSchema());
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

    private static void doApplyMigration(DatasourceInfo dsInfo, SpringLiquibase lqb, Map<String, String> changeLogParams) {
        @SuppressWarnings("PMD.CloseResource")
        DataSource ds = dsInfo.getDataSource();
        String schema = null;
        if (ds instanceof HikariDataSource) {
            schema = ((HikariDataSource) ds).getSchema();
        }else if (ds instanceof HikariDS) {
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
                boolean isTestDb = ((HikariDataSource) lqb.getDataSource()).getJdbcUrl().startsWith("jdbc:h2:mem");
                if (!isTestDb) {
                    LOG.warn("Looks like migrations are being ran twice for %s.%s, ignore this error", dsInfo.getName(), schema);
                }
            } else {
                throw new DatabaseException(e, "Migration failed for %s", schema);
            }
        }
    }

    public static String findChangeLogPath(String applicationName, DataSourceConfig config) {
        String changelogPath = null;
        boolean hasMigration = !("false".equals(config.getMigration()) || "no".equals(config.getMigration()));
        if (hasMigration) {
            if (TextUtil.isNotEmpty(config.getMigration()) && !"true".equals(config.getMigration())) {
                changelogPath = "/db/changelog/" + config.getMigration() + ".xml";
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
            // Will ignore because the table might have been created by another instance of the service
            LOG.warn(e.getMessage(), e);
        }
        return lockProvider;
    }

}
