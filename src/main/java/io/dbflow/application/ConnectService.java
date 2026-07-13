package io.dbflow.application;

import io.dbflow.common.exception.ValidationException;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.dbms.DbConnection;
import io.dbflow.infrastructure.dbms.DbConnectionFactory;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import io.dbflow.infrastructure.security.CredentialSecurity;
import io.dbflow.common.security.StringEncryptor;

import java.util.List;

public class ConnectService {

    private final DbConfigRepository dbConfigRepository;
    private final DbConnectionFactory dbConnectionFactory;
    private final StringEncryptor passwordEncryptor;

    public ConnectService(DbConfigRepository dbConfigRepository) {
        this(dbConfigRepository, new DbConnectionFactory(), CredentialSecurity.stringEncryptor());
    }

    public ConnectService(
            DbConfigRepository dbConfigRepository,
            DbConnectionFactory dbConnectionFactory,
            StringEncryptor passwordEncryptor
    ) {
        this.dbConfigRepository = dbConfigRepository;
        this.dbConnectionFactory = dbConnectionFactory;
        this.passwordEncryptor = passwordEncryptor;
    }

    public void saveDbConnect(DbConfig dbConfig) {
        DbConnection dbConnection = getDbConnection(dbConfig.getDbType());

        dbConnection.testConnection(dbConfig);
        dbConfig.setDbPassword(passwordEncryptor.encrypt(dbConfig.getDbPassword()));
        dbConfigRepository.saveDbConfig(dbConfig);
    }

    public void updateDbConfig(DbConfig dbConfig) {
        DbConnection dbConnection = getDbConnection(dbConfig.getDbType());

        dbConnection.testConnection(dbConfig);
        dbConfig.setDbPassword(passwordEncryptor.encrypt(dbConfig.getDbPassword()));
        dbConfigRepository.updateDbConfig(dbConfig);
    }

    public List<DbConfig> findDbConfigs() {
        return dbConfigRepository.findDbConfigs();
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
            throw new ValidationException(ValidationException.DUPLICATED_DB_ALIAS);
        }
    }

    private DbConnection getDbConnection(String dbType) {
        return dbConnectionFactory.create(dbType);
    }
}
