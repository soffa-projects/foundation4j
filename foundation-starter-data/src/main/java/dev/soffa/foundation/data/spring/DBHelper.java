package dev.soffa.foundation.data.spring;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Properties;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.error.DatabaseException;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.jdbi.v3.core.Jdbi;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static dev.soffa.foundation.data.common.ExtDataSource.TENANT_PLACEHOLDER;

public final class DBHelper {


    private DBHelper() {
    }

    public static DataSource createDataSource(String name, String url) {
        return createDataSource(name, ExtDataSource.create("application", name, url));
    }

    public static DataSource createDataSource(String applicationName, String name, String url) {
        return createDataSource(name, ExtDataSource.create(applicationName, name, url));
    }

    @SneakyThrows
    public static DataSource createDataSource(ExtDataSource config) {
        return createDataSource(config.getName(), config);
    }

    @SneakyThrows
    public static DataSource createDataSource(String sname, ExtDataSource config) {
        String name = sname;
        if (TextUtil.isEmpty(name)) {
            name = config.getName();
        }

        boolean isH2 = config.getUrl().contains(":h2:");

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

        Logger.platform.debug("Using jdbcUrl: %s", config.getUrl());

        if (isH2) {
            hc.addDataSourceProperty("ignore_startup_parameters", "search_path");
            if (config.hasSchema()) {
                config.setSchema(config.getSchema().toUpperCase());
            }
        }

        hc.addDataSourceProperty("autoReconnect", true);
        hc.addDataSourceProperty("cachePrepStmts", true);
        hc.addDataSourceProperty("prepStmtCacheSize", 250);
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hc.addDataSourceProperty("useServerPrepStmts", true);
        hc.addDataSourceProperty("cacheResultSetMetadata", true);



        if (!isH2 && config.hasSchema() && !TENANT_PLACEHOLDER.equalsIgnoreCase(config.getSchema())) {
            Jdbi.create(config.getUrl()).useTransaction(handle -> {
                //EL
                String command = "CREATE SCHEMA IF NOT EXISTS " + config.getSchema();
                int res = handle.execute(command);
                Logger.platform.debug("Schema creation result: %s --> %s", config.getSchema(), res);
            });
            hc.setSchema(config.getSchema());
        }
        return new HikariDataSource(hc);
    }


    public static String findChangeLogPath(String applicationName, String migrationName) {
        if (TextUtil.isEmpty(migrationName) || migrationName.matches("no|yes|0|disabled")) {
            return null;
        }
        String changelogPath;
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
            Logger.platform.warn(e.getMessage());
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
            Logger.platform.warn(e.getMessage());
            throw new DatabaseException(e);
        }
    }

}
