package io.dbflow.infrastructure.dbms;

import io.dbflow.common.exception.ValidationException;

public class DbConnectionFactory {

    public DbConnection create(String dbType) {
        return switch (dbType) {
            case "POSTGRESQL" -> new PostgresDbConnection();
            default -> throw new ValidationException(ValidationException.UNSUPPORTED_DBMS);
        };
    }
}
