package io.dbflow.infrastructure.external.repository;

import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;
import io.dbflow.domain.TableSnapshot;

import java.util.List;

public interface MetadataCollector {
    List<TableMetadata> collectTableSnapshotList(DbConfig dbConfig);
    List<ColumnMetadata> collectColumnSnapshotList(DbConfig dbConfig, List<TableSnapshot> tableSnapshot);
}
