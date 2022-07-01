package dev.soffa.foundation.data.jdbi;

import org.jdbi.v3.core.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class MultiTenantConnectionFactory implements ConnectionFactory {


    @Override
    public Connection openConnection() throws SQLException {
        return null;
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        ConnectionFactory.super.closeConnection(conn);
    }

}
