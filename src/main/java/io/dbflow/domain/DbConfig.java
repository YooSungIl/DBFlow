package io.dbflow.domain;

import java.time.OffsetDateTime;

public class DbConfig {
    private Long dbConfigId;
    private String dbAlias;
    private String dbType;
    private String dbHost;
    private Integer dbPort;
    private String dbName;
    private String dbSchema;
    private String dbUser;
    private String dbPassword;
    private Integer useYn;
    private String createdAt;
    private String updatedAt;

    String now = OffsetDateTime.now().toString();

    public DbConfig() {}

    public DbConfig(String dbAlias, String dbType, String dbHost, Integer dbPort, String dbName, String dbSchema, String dbUser, String dbPassword) {
        this.dbAlias = dbAlias;
        this.dbType = dbType;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbSchema = dbSchema;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.useYn = 1;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public DbConfig(Long dbConfigId, String dbAlias, String dbType, String dbHost, Integer dbPort, String dbName, String dbSchema, String dbUser, String dbPassword, Integer useYn) {
        this.dbConfigId = dbConfigId;
        this.dbAlias = dbAlias;
        this.dbType = dbType;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbSchema = dbSchema;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.useYn = useYn;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public DbConfig(String dbAlias) {
        this.dbAlias = dbAlias;
        this.updatedAt = now;
    }

    public Long getDbConfigId() {
        return dbConfigId;
    }

    public void setDbConfigId(Long dbConfigId) {
        this.dbConfigId = dbConfigId;
    }

    public String getDbAlias() {
        return dbAlias;
    }

    public void setDbAlias(String dbAlias) {
        this.dbAlias = dbAlias;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public Integer getDbPort() {
        return dbPort;
    }

    public void setDbPort(Integer dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public Integer getUseYn() {
        return useYn;
    }

    public void setUseYn(Integer useYn) {
        this.useYn = useYn;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdateAt() {
        return updatedAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updatedAt = updateAt;
    }
}
