package io.dbflow.application;

import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.ColumnSnapshot;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;
import io.dbflow.domain.TableSnapshot;
import io.dbflow.infrastructure.external.repository.MetadataCollector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataCollectServiceTest {

    @Test
    void 수집한_테이블_메타데이터를_Snapshot_Service에_전달한다() {
        TableMetadata metadata = new TableMetadata();
        metadata.setTableName("member");
        StubSnapshotService snapshotService = new StubSnapshotService(List.of());
        MetadataCollectService service = new MetadataCollectService(new StubMetadataCollector(List.of(metadata), List.of()), snapshotService);

        service.tableCollect(dbConfig());

        assertEquals(100L, snapshotService.insertedDbConfigId);
        assertEquals(List.of(metadata), snapshotService.insertedTableMetadata);
    }

    @Test
    void 컬럼_메타데이터를_부모_테이블_ID와_연결해_저장한다() {
        TableSnapshot table = new TableSnapshot();
        table.setTableSnapshotId(1L);
        table.setTableName("member");
        ColumnMetadata metadata = columnMetadata("member", "member_id");
        StubSnapshotService snapshotService = new StubSnapshotService(List.of(table));
        MetadataCollectService service = new MetadataCollectService(new StubMetadataCollector(List.of(), List.of(metadata)), snapshotService);

        service.columnCollect(dbConfig());

        ColumnSnapshot column = snapshotService.insertedColumns.get(0);
        assertEquals(1L, column.getTableSnapshotId());
        assertEquals("member_id", column.getColumnName());
        assertEquals("bigint", column.getDataType());
    }

    @Test
    void 부모_테이블이_없는_컬럼은_저장하지_않는다() {
        ColumnMetadata metadata = columnMetadata("missing", "unknown_id");
        StubSnapshotService snapshotService = new StubSnapshotService(List.of());
        MetadataCollectService service = new MetadataCollectService(new StubMetadataCollector(List.of(), List.of(metadata)), snapshotService);

        assertThrows(IllegalStateException.class, () -> service.columnCollect(dbConfig()));
    }

    private DbConfig dbConfig() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbConfigId(100L);
        return dbConfig;
    }

    private ColumnMetadata columnMetadata(String tableName, String columnName) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setTableName(tableName);
        metadata.setColumnName(columnName);
        metadata.setColumnComment("ID");
        metadata.setColumnOrder(1);
        metadata.setDataType("bigint");
        metadata.setDataLength(64);
        metadata.setNullableYn(0);
        return metadata;
    }

    private static class StubMetadataCollector implements MetadataCollector {
        private final List<TableMetadata> tables;
        private final List<ColumnMetadata> columns;

        private StubMetadataCollector(List<TableMetadata> tables, List<ColumnMetadata> columns) {
            this.tables = tables;
            this.columns = columns;
        }

        @Override
        public List<TableMetadata> collectTableSnapshotList(DbConfig dbConfig) {
            return tables;
        }

        @Override
        public List<ColumnMetadata> collectColumnSnapshotList(DbConfig dbConfig, List<TableSnapshot> tableSnapshot) {
            return columns;
        }
    }

    private static class StubSnapshotService extends SnapshotService {
        private final List<TableSnapshot> collectTables;
        private Long insertedDbConfigId;
        private List<TableMetadata> insertedTableMetadata;
        private List<ColumnSnapshot> insertedColumns;

        private StubSnapshotService(List<TableSnapshot> collectTables) {
            this.collectTables = collectTables;
        }

        @Override
        public void insertTableSnapshotList(Long dbConfigId, List<TableMetadata> tableMetadataList) {
            insertedDbConfigId = dbConfigId;
            insertedTableMetadata = tableMetadataList;
        }

        @Override
        public List<TableSnapshot> selectCollectTableSnapshot(Long dbConfigId) {
            return collectTables;
        }

        @Override
        public void insertColumnSnapshotList(List<ColumnSnapshot> columnSnapshotList) {
            insertedColumns = columnSnapshotList;
        }
    }
}
