package io.dbflow.domain;

import io.dbflow.common.enums.SnapshotType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotTest {

    @Test
    void 컬럼을_동일한_테이블_스냅샷에_연결한다() {
        TableSnapshot table = table(1L, "member");
        ColumnSnapshot column = column(10L, 1L, "member_id");

        Snapshot snapshot = new Snapshot(
                SnapshotType.COLLECT,
                100L,
                null,
                List.of(table),
                List.of(column)
        );

        assertEquals(SnapshotType.COLLECT, snapshot.getSnapshotType());
        assertEquals(100L, snapshot.getDbConfigId());
        assertEquals(1, snapshot.getTables().size());
        assertSame(column, snapshot.getTables().get(0).getColumns().get(0));
    }

    @Test
    void 부모_테이블이_없는_컬럼은_허용하지_않는다() {
        ColumnSnapshot column = column(10L, 999L, "member_id");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new Snapshot(SnapshotType.COLLECT, 100L, null, List.of(), List.of(column))
        );

        assertTrue(exception.getMessage().contains("tableSnapshotId=999"));
    }

    @Test
    void 중복된_테이블_스냅샷_ID는_허용하지_않는다() {
        TableSnapshot first = table(1L, "member");
        TableSnapshot second = table(1L, "orders");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new Snapshot(SnapshotType.CURRENT, 100L, null, List.of(first, second), List.of())
        );

        assertTrue(exception.getMessage().contains("tableSnapshotId=1"));
    }

    private TableSnapshot table(Long id, String name) {
        TableSnapshot table = new TableSnapshot();
        table.setTableSnapshotId(id);
        table.setTableName(name);
        return table;
    }

    private ColumnSnapshot column(Long id, Long tableId, String name) {
        ColumnSnapshot column = new ColumnSnapshot();
        column.setColumnSnapshotId(id);
        column.setTableSnapshotId(tableId);
        column.setColumnName(name);
        return column;
    }
}
