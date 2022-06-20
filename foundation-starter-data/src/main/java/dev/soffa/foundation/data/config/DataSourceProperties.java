package dev.soffa.foundation.data.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.model.TenantId;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DataSourceProperties {

    public static final String H2_DRIVER = "org.h2.Driver";
    public static final String H2 = "h2";
    public static final String PG = "postgresql";
    private static final Logger LOG = Logger.get(DataSourceProperties.class);
    private boolean defaultSource;
    private String name;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schema;
    private Map<String, String> properties;
    private List<String> migrations;

    @SneakyThrows
    public static DataSourceProperties create(final String applicationName, final String name, final String datasourceUrl) {

        String databaseUrl = datasourceUrl.trim();
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

        return DataSourceProperties.builder()
            .name(applicationName + "_" + name)
            .username(jdbcInfo.getUsername())
            .password(jdbcInfo.getPassword())
            .schema(schema)
            .defaultSource(TenantId.DEFAULT_VALUE.equalsIgnoreCase(name))
            .url(jdbcInfo.getUrl())
            .driverClassName(jdbcInfo.getDriver())
            .properties(urlInfo.getParams())
            .build();
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
                jdbcUrl.append(";INIT=CREATE SCHEMA IF NOT EXISTS ").append(schema);
            }
        } else {
            jdbcDriver = "org.postgresql.Driver";
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

    /*
    private static void createSchema(String jdbcUrl, String username, String password, String schema) {
        // Automatic schema creation if possible
        Jdbi jdbi;
        if (username != null) {
            jdbi = Jdbi.create(jdbcUrl, username, password);
        } else {
            jdbi = Jdbi.create(jdbcUrl);
        }
        jdbi.inTransaction(handle -> {
            handle.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            return null;
        });
    }

     */

    public boolean hasSchema() {
        return TextUtil.isNotEmpty(schema);
    }

    public String property(String name) {
        return properties.get(name);
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
