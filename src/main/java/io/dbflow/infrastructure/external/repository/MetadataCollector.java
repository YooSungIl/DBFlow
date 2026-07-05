package io.dbflow.infrastructure.external.repository;

import io.dbflow.domain.CollectTableSnapshot;
import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;

import java.util.List;

public interface MetadataCollector {
    List<TableMetadata> collectTableSnapshotList(DbConfig dbConfig);
    List<ColumnMetadata> collectColumnSnapshotList(DbConfig dbConfig, List<CollectTableSnapshot> tableSnapshot);
}
