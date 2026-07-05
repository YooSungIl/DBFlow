package io.dbflow.infrastructure.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.dbflow.common.Exception.DbConnectionException;
import io.dbflow.domain.DbConfig;

public class PostgresDbConnection implements DbConnection{

    @Override
    public void testConnection(DbConfig config) {
        try {
            Class.forName("org.postgresql.Driver");

            try (Connection connection = getConnection(config)) {
                System.out.println("PostgreSQL JDBC Driver Connected successfully");
            }
        } catch (ClassNotFoundException e) {
            throw new DbConnectionException("PostgreSQL JDBC Driver를 찾을 수 없습니다.", e);
        } catch (SQLException e) {
            throw new DbConnectionException("DB 연결 실패 : " + e.getMessage());
        }
    }

    private Connection getConnection(DbConfig config) throws SQLException {

        String url = "jdbc:postgresql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName();

        return DriverManager.getConnection(url, config.getDbUser(), config.getDbPassword());
    }
}
