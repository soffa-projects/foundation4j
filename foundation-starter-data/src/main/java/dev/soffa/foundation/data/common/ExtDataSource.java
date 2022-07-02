package dev.soffa.foundation.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.DigestUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.data.DataSourceConfig;
import dev.soffa.foundation.data.spring.DBHelper;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.model.TenantId;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Data
@Builder
public class ExtDataSource implements DataSource {

    private static final Map<String, DataSource> UNIQDS = new ConcurrentHashMap<>();
    public static final String TENANT_PLACEHOLDER = "__tenant__";

    public static final String H2_DRIVER = "org.h2.Driver";
    public static final String PG_DRIVER = "org.postgresql.Driver";
    public static final String H2 = "h2";
    public static final String PG = "postgresql";
    private String applicationName;
    private String name;
    private String id;
    private String baseName;
    private String baseUrl;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schema;
    private Map<String, String> properties;
    private List<String> migrations;

    private String changeLogPath;
    private String tablesPrefix;

    private transient boolean initialized = false;

    @JsonIgnore
    private transient DataSource dataSource;
    @JsonIgnore
    private boolean migrated;
    // private LocalContainerEntityManagerFactoryBean em;


    // private PlatformTransactionManager tx;

    @JsonIgnore
    public boolean isDefault() {
        return TenantId.DEFAULT_VALUE.equalsIgnoreCase(baseName);
    }

    /*
    public void configureTx(EntityManagerFactoryBuilder builder, String... packages) {
        this.em = builder.dataSource(dataSource).packages(packages)
            .persistenceUnit(name)
            .build();
        // this.tx = new JpaTransactionManager(Objects.requireNonNull(this.em.getObject()));
    }
     */


    public boolean isTenantTemplate() {
        return hasTenantPlaceHolder(this.url) || hasTenantPlaceHolder(schema);
    }

    public ExtDataSource ofTenant(String tenant) {
        if (!isTenantTemplate()) {
            return this;
        }

        return ExtDataSource.builder()
            .name(applicationName + "_" + tenant)
            .baseName(this.baseName)
            .id(tenant)
            .baseUrl(baseUrl)
            .applicationName(applicationName)
            .username(replaceTenantPlaceHolder(this.username, tenant))
            .password(replaceTenantPlaceHolder(this.password, tenant))
            .tablesPrefix(replaceTenantPlaceHolder(this.tablesPrefix, tenant))
            .changeLogPath(replaceTenantPlaceHolder(this.changeLogPath, tenant))
            .schema(replaceTenantPlaceHolder(this.schema, tenant))
            .url(replaceTenantPlaceHolder(this.url, tenant))
            .driverClassName(this.driverClassName)
            .properties(this.properties)
            .build()
            .afterPropertiesSet();
    }

    public void setMigrated(boolean value) {
        this.migrated = value;
    }

    private static String replaceTenantPlaceHolder(String input, String value) {
        if (TextUtil.isEmpty(input)) {
            return input;
        }
        return input.replace(TENANT_PLACEHOLDER, value).replace(TENANT_PLACEHOLDER.toUpperCase(), value);
    }

    private static boolean hasTenantPlaceHolder(String input) {
        if (TextUtil.isEmpty(input)) {
            return false;
        }
        return input.contains(TENANT_PLACEHOLDER) || input.contains(TENANT_PLACEHOLDER.toUpperCase());
    }

    public static ExtDataSource create(@NonNull final String applicationName, @NonNull final String name, @NonNull final String url) {
        return create(applicationName, new DataSourceConfig(name, url));
    }

    public static ExtDataSource create(String name, String url, ExtDataSource config) {
        DataSourceConfig entry = new DataSourceConfig(
            name, config.getTablesPrefix(), url, config.getChangeLogPath()
        );
        return create(config.getApplicationName(), entry);
    }

    private DataSource getDataSource() {
        if (this.url.contains(TENANT_PLACEHOLDER) || TENANT_PLACEHOLDER.equalsIgnoreCase(schema)) {
            throw new TechnicalException("This datasource cannot be called directly, use ofTenant() instead");
        }
        if (this.dataSource == null) {
            String fingerprint = DigestUtil.md5(baseUrl);
            if (UNIQDS.containsKey(fingerprint)) {
                this.dataSource = UNIQDS.get(fingerprint);
            } else {
                this.dataSource = DBHelper.createDataSource(this.name, this);
                UNIQDS.put(fingerprint, this.dataSource);
            }
            createSchema(this.dataSource, this.getSchema());
        }
        return this.dataSource;
    }

    private ExtDataSource afterPropertiesSet() {
        if (isTenantTemplate()) {
            initialized = true;
            return this;
        }
        createSchema(this.getDataSource(), this.getSchema());
        initialized =true;
        return this;
    }

    private static void createSchema(DataSource ds, String schema) {
        if (TextUtil.isEmpty(schema)) {
            return;
        }
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.execute("create schema if not exists " + schema);
    }


    public static ExtDataSource create(@NonNull final String applicationName, @NonNull final DataSourceConfig entry) {

        Preconditions.checkNotNull(entry.getName(), "DataSourceConfig.name cannot be null");
        Preconditions.checkNotNull(entry.getUrl(), "DataSourceConfig.url cannot be null");

        String databaseUrl = entry.getUrl().trim();
        String baseUrl = databaseUrl.split("\\?")[0].toLowerCase();
        String provider;

        if (databaseUrl.startsWith("h2://")) {
            provider = H2;
        } else if (databaseUrl.matches("^(pg|postgres(ql)?)://.*")) {
            provider = PG;
        } else {
            throw new TechnicalException("Database protocol not implemented yet: " + databaseUrl);
        }

        UrlInfo urlInfo = UrlInfo.parse(databaseUrl);
        String schema = urlInfo.param("schema", null);
        if (schema != null) {
            if (provider.equals(H2)) {
                schema = schema.toUpperCase();
            } else {
                schema = schema.toLowerCase();
            }
        }

        JdbcInfo jdbcInfo = createJdbcUrl(applicationName, provider, urlInfo, schema);

        return ExtDataSource.builder()
            .name(applicationName + "_" + entry.getName())
            .baseName(entry.getName())
            .id(entry.getName())
            .baseUrl(baseUrl)
            .applicationName(applicationName)
            .username(jdbcInfo.getUsername())
            .password(jdbcInfo.getPassword())
            .tablesPrefix(entry.getTablesPrefix())
            .changeLogPath(entry.getMigration())
            .schema(schema)
            .url(jdbcInfo.getUrl())
            .driverClassName(jdbcInfo.getDriver())
            .properties(urlInfo.getParams())
            .build()
            .afterPropertiesSet();
    }

    @SneakyThrows
    private static JdbcInfo createJdbcUrl(String applicationName, String provider, UrlInfo url, String schema) {
        StringBuilder jdbcUrl = new StringBuilder();
        String jdbcDriver;
        StringBuilder hostname = new StringBuilder(url.getHostname());
        String path = url.getPath().replaceAll("^/", "");
        if (H2.equals(provider)) {
            jdbcDriver = H2_DRIVER;
            jdbcUrl.append(String.format("jdbc:h2:%1$s:%2$s;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE", hostname, path));
            if (TextUtil.isNotEmpty(schema)) {
                jdbcUrl.append(";INIT=CREATE SCHEMA IF NOT EXISTS ").append(schema).append("\\;SET SCHEMA ").append(schema);
            }
        } else {
            jdbcDriver = PG_DRIVER;
            if (url.getPort() == -1) {
                hostname.append(":5432");
            } else {
                hostname.append(':').append(url.getPort());
            }
            jdbcUrl.append(String.format("jdbc:postgresql://%1$s/%2$s", hostname, path));
            if (TextUtil.isNotEmpty(schema)) {
                // createSchema(jdbcUrl.toString(), url.getUsername(), url.getPassword(), schema);
                jdbcUrl.append("?currentSchema=").append(schema).append('&');
            } else {
                jdbcUrl.append('?');
            }
            jdbcUrl.append("ApplicationName=").append(applicationName);
        }

        return new JdbcInfo(jdbcDriver, jdbcUrl.toString(), url.getUsername(), url.getPassword(), schema);
    }

    @JsonIgnore
    public boolean hasSchema() {
        return TextUtil.isNotEmpty(schema);
    }

    public String property(String name) {
        return properties.get(name);
    }

    @Override
    @JsonIgnore
    public Connection getConnection() throws SQLException {
        return withSchema(this.getDataSource().getConnection());
    }

    @Override
    @JsonIgnore
    public Connection getConnection(String username, String password) throws SQLException {
        return withSchema(this.getDataSource().getConnection(username, password));
    }

    @SneakyThrows
    private Connection withSchema(Connection cnx) {
        if (!initialized) {
            return cnx;
        }
        if (cnx != null && TextUtil.isNotEmpty(schema)) {
            if (isH2()) {
                cnx.setSchema(schema.toUpperCase());
            }else {
                cnx.setSchema(schema);
            }
        }
        return cnx;
    }

    @Override
    @JsonIgnore
    public PrintWriter getLogWriter() throws SQLException {
        return this.getDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.getDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.getDataSource().setLoginTimeout(seconds);
    }

    @Override
    @JsonIgnore
    public int getLoginTimeout() throws SQLException {
        return this.getDataSource().getLoginTimeout();
    }

    @Override
    @JsonIgnore
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.getDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.getDataSource().unwrap(iface);
    }

    @SneakyThrows
    @Override
    @JsonIgnore
    public boolean isWrapperFor(Class<?> iface) {
        return this.getDataSource().isWrapperFor(iface);
    }

    @JsonIgnore
    public boolean isH2() {
        return H2_DRIVER.equals(this.driverClassName);
    }

    public boolean isPG() {
        return PG_DRIVER.equalsIgnoreCase(this.driverClassName);

    }


    @Value
    private static class JdbcInfo {
        String driver;
        String url;
        String username;
        String password;
        String schema;
    }

}
