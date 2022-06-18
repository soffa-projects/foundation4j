package dev.soffa.foundation.data.common;

import com.zaxxer.hikari.HikariDataSource;
import dev.soffa.foundation.commons.TextUtil;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.AbstractDataSource;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class HikariDS extends AbstractDataSource {

    private final HikariDataSource internal;
    private final String schema;

    public String getSchema() {
        return schema;
    }

    public HikariDataSource unwrap() {
        return internal;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection cnx = internal.getConnection();
        if (TextUtil.isNotEmpty(schema)) {
            cnx.setSchema(schema);
        }
        return cnx;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection cnx = internal.getConnection(username, password);
        if (TextUtil.isNotEmpty(schema)) {
            cnx.setSchema(schema);
        }
        return cnx;
    }
}
