package io.dbflow.infrastructure.dbms;

import io.dbflow.domain.DbConfig;

public interface DbConnection {
    void testConnection(DbConfig dbConfig);
}
