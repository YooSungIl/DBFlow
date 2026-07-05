package io.dbflow.application;

import io.dbflow.common.Exception.ValidationException;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.dbms.DbConnection;
import io.dbflow.infrastructure.dbms.PostgresDbConnection;
import io.dbflow.infrastructure.repository.DbConfigRepository;

import java.util.List;

public class ConnectService {

    private final DbConfigRepository dbConfigRepository;

    public ConnectService(DbConfigRepository dbConfigRepository) {
        this.dbConfigRepository = dbConfigRepository;
    }

    public void saveDbConnect(DbConfig dbConfig) {
        DbConnection dbConnection = getDbConnection(dbConfig.getDbType());

        dbConnection.testConnection(dbConfig);
        dbConfigRepository.saveOnlyOne(dbConfig);
    }

    public void updateDbConfig(DbConfig dbConfig) {
        DbConnection dbConnection = getDbConnection(dbConfig.getDbType());

        dbConnection.testConnection(dbConfig);
        dbConfigRepository.updateDbConfig(dbConfig);
    }

    public List<DbConfig> findDbConfigList() {
        return dbConfigRepository.findDbConfigList();
    }

    public DbConfig findDbConfig(String dbAlias) {
        return dbConfigRepository.findDbConfig(dbAlias);
    }

    public void deleteDbConfig(DbConfig dbConfig) {
        dbConfigRepository.deleteDbConfig(dbConfig);
    }

    public void validateNewAlias(String alias) {

        CommonValidation.validateAlias(alias);

        DbConfig exists = dbConfigRepository.findDbConfig(alias);

        if (exists != null) {
            throw new ValidationException("이미 존재하는 DB 별칭입니다.");
        }
    }

    private DbConnection getDbConnection(String dbType) {
        return switch (dbType) {
            case "POSTGRESQL" -> new PostgresDbConnection();
            default -> throw new ValidationException("지원하지 않는 DBMS입니다.");
        };
    }
}
