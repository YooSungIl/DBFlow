package io.dbflow.infrastructure.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.dbflow.common.Exception.ServiceException;
import io.dbflow.domain.DbConfig;

public class PostgresDbConnection implements DbConnection{

    @Override
    public void testConnection(DbConfig config) {
        try {
            Class.forName("org.postgresql.Driver");

            try (Connection connection = getConnection(config)) {
                connection.isValid(3);
            }
        } catch (ClassNotFoundException e) {
            throw new ServiceException("PostgreSQL JDBC Driver를 찾을 수 없습니다.", e);
        } catch (SQLException e) {
            throw new ServiceException("DB 연결 실패 : " + e.getMessage(), e);
        }
    }

    private Connection getConnection(DbConfig config) throws SQLException {

        String url = "jdbc:postgresql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName();

        return DriverManager.getConnection(url, config.getDbUser(), config.getDbPassword());
    }
}
