package io.dbflow.application;

import io.dbflow.common.enums.SnapshotType;
import io.dbflow.domain.ColumnSnapshot;
import io.dbflow.domain.Snapshot;
import io.dbflow.domain.TableSnapshot;
import io.dbflow.infrastructure.repository.SnapshotRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SnapshotServiceTest {

    @Test
    void Collect_Snapshot을_조회하고_테이블과_컬럼을_조립한다() {
        TableSnapshot table = table(1L);
        ColumnSnapshot column = column(10L, 1L);
        SnapshotService service = new SnapshotService(new StubSnapshotRepository(List.of(table), List.of(column), List.of(), List.of()));

        Snapshot snapshot = service.findCollectSnapshot(100L);

        assertEquals(SnapshotType.COLLECT, snapshot.getSnapshotType());
        assertEquals(100L, snapshot.getDbConfigId());
        assertSame(column, snapshot.getTables().get(0).getColumns().get(0));
    }

    @Test
    void Current_Snapshot을_조회하고_테이블과_컬럼을_조립한다() {
        TableSnapshot table = table(2L);
        ColumnSnapshot column = column(20L, 2L);
        SnapshotService service = new SnapshotService(new StubSnapshotRepository(List.of(), List.of(), List.of(table), List.of(column)));

        Snapshot snapshot = service.findCurrentSnapshot(100L);

        assertEquals(SnapshotType.CURRENT, snapshot.getSnapshotType());
        assertSame(column, snapshot.getTables().get(0).getColumns().get(0));
    }

    private TableSnapshot table(Long id) {
        TableSnapshot table = new TableSnapshot();
        table.setTableSnapshotId(id);
        table.setTableName("member");
        return table;
    }

    private ColumnSnapshot column(Long id, Long tableId) {
        ColumnSnapshot column = new ColumnSnapshot();
        column.setColumnSnapshotId(id);
        column.setTableSnapshotId(tableId);
        column.setColumnName("member_id");
        return column;
    }

    private static class StubSnapshotRepository extends SnapshotRepository {
        private final List<TableSnapshot> collectTables;
        private final List<ColumnSnapshot> collectColumns;
        private final List<TableSnapshot> currentTables;
        private final List<ColumnSnapshot> currentColumns;

        private StubSnapshotRepository(
                List<TableSnapshot> collectTables,
                List<ColumnSnapshot> collectColumns,
                List<TableSnapshot> currentTables,
                List<ColumnSnapshot> currentColumns
        ) {
            this.collectTables = collectTables;
            this.collectColumns = collectColumns;
            this.currentTables = currentTables;
            this.currentColumns = currentColumns;
        }

        @Override
        public List<TableSnapshot> selectCollectTableSnapshot(Long dbConfigId) {
            return collectTables;
        }

        @Override
        public List<ColumnSnapshot> selectCollectColumnSnapshot(Long dbConfigId) {
            return collectColumns;
        }

        @Override
        public List<TableSnapshot> selectCurrentTableSnapshot(Long dbConfigId) {
            return currentTables;
        }

        @Override
        public List<ColumnSnapshot> selectCurrentColumnSnapshot(Long dbConfigId) {
            return currentColumns;
        }
    }
}
