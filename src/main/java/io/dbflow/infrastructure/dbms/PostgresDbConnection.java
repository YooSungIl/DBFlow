package io.dbflow.infrastructure.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.dbflow.common.exception.ServiceException;
import io.dbflow.common.security.StringEncryptor;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.security.CredentialSecurity;

public class PostgresDbConnection implements DbConnection {

    private final StringEncryptor passwordEncryptor;

    public PostgresDbConnection() {
        this(CredentialSecurity.stringEncryptor());
    }

    public PostgresDbConnection(StringEncryptor passwordEncryptor) {
        this.passwordEncryptor = passwordEncryptor;
    }

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
            throw new ServiceException("DB 연결에 실패했습니다.", e);
        }
    }

    private Connection getConnection(DbConfig config) throws SQLException {

        String url = "jdbc:postgresql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName();

        String password = resolvePasswordForConnection(config);
        return DriverManager.getConnection(url, config.getDbUser(), password);
    }

    String resolvePasswordForConnection(DbConfig config) {
        return passwordEncryptor.decrypt(config.getDbPassword());
    }
}
