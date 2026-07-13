package io.dbflow.infrastructure.repository.mapper;

import io.dbflow.domain.DbConfig;

import java.util.List;

public interface DbConfigMapper {

    void insert(DbConfig dbConfig);

    List<DbConfig> findDbConfigs();

    DbConfig findDbConfig(String dbAlias);

    int deleteDbConfig(DbConfig dbConfig);

    int updateDbConfig(DbConfig dbConfig);
}
