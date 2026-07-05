package io.dbflow.infrastructure.external.repository.postgres.mapper;

import io.dbflow.domain.CollectTableSnapshot;
import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostgresMetadataCollectorMapper {
    List<TableMetadata> collectTableSnapshotList(DbConfig dbConfig);
    List<ColumnMetadata> collectColumnSnapshotList(@Param("dbConfig") DbConfig dbConfig, @Param("tables") List<CollectTableSnapshot> tables);
}
