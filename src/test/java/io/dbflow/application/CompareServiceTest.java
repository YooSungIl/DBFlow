package io.dbflow.application;

import io.dbflow.common.enums.ChangeColumn;
import io.dbflow.common.enums.ChangeType;
import io.dbflow.common.enums.SnapshotType;
import io.dbflow.domain.ColumnSnapshot;
import io.dbflow.domain.Snapshot;
import io.dbflow.domain.TableSnapshot;
import io.dbflow.domain.WorkChange;
import io.dbflow.domain.WorkComponent;
import io.dbflow.domain.WorkDiffResult;
import io.dbflow.domain.WorkTarget;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompareServiceTest {

    private static final Long DB_CONFIG_ID = 100L;

    @Test
    void 동일한_스냅샷은_변경사항이_없다() {
        TableSnapshot collectTable = table(1L, "member", "회원");
        ColumnSnapshot collectColumn = column(11L, 1L, "member_id");
        TableSnapshot currentTable = table(2L, "member", "회원");
        ColumnSnapshot currentColumn = column(21L, 2L, "member_id");

        WorkDiffResult result = compare(
                snapshot(SnapshotType.COLLECT, List.of(collectTable), List.of(collectColumn)),
                snapshot(SnapshotType.CURRENT, List.of(currentTable), List.of(currentColumn))
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void 테이블의_추가_수정_삭제를_감지한다() {
        TableSnapshot added = table(1L, "added_table", "추가");
        TableSnapshot modifiedCollect = table(2L, "modified_table", "변경 후");
        TableSnapshot modifiedCurrent = table(3L, "modified_table", "변경 전");
        TableSnapshot deleted = table(4L, "deleted_table", "삭제");

        WorkDiffResult result = compare(
                snapshot(SnapshotType.COLLECT, List.of(added, modifiedCollect), List.of()),
                snapshot(SnapshotType.CURRENT, List.of(modifiedCurrent, deleted), List.of())
        );

        assertEquals(3, result.getTargets().size());
        assertEquals(ChangeType.ADD.name(), target(result, "added_table").getChangeType());
        assertEquals(ChangeType.MOD.name(), target(result, "modified_table").getChangeType());
        assertEquals(ChangeType.DEL.name(), target(result, "deleted_table").getChangeType());

        WorkChange commentChange = target(result, "modified_table")
                .getComponents().get(0)
                .getChanges().get(0);
        assertEquals(ChangeColumn.TABLE_COMMENT.name(), commentChange.getChangeColumn());
        assertEquals("변경 전", commentChange.getBeforeValue());
        assertEquals("변경 후", commentChange.getAfterValue());
    }

    @Test
    void 컬럼의_추가와_삭제를_감지한다() {
        TableSnapshot collectTable = table(1L, "member", "회원");
        TableSnapshot currentTable = table(2L, "member", "회원");
        ColumnSnapshot maintainedCollect = column(11L, 1L, "member_id");
        ColumnSnapshot added = column(12L, 1L, "email");
        ColumnSnapshot maintainedCurrent = column(21L, 2L, "member_id");
        ColumnSnapshot deleted = column(22L, 2L, "phone");

        WorkDiffResult result = compare(
                snapshot(SnapshotType.COLLECT, List.of(collectTable), List.of(maintainedCollect, added)),
                snapshot(SnapshotType.CURRENT, List.of(currentTable), List.of(maintainedCurrent, deleted))
        );

        WorkTarget target = target(result, "member");
        assertEquals(DB_CONFIG_ID, target.getDbConfigId());
        assertEquals(ChangeType.MOD.name(), target.getChangeType());
        assertEquals(ChangeType.ADD.name(), component(target, "email").getChangeType());
        assertEquals(ChangeType.DEL.name(), component(target, "phone").getChangeType());
    }

    @Test
    void 컬럼의_모든_속성_변경을_감지한다() {
        TableSnapshot collectTable = table(1L, "member", "회원");
        TableSnapshot currentTable = table(2L, "member", "회원");
        ColumnSnapshot collect = column(11L, 1L, "name");
        ColumnSnapshot current = column(21L, 2L, "name");

        collect.setColumnOrder(2);
        collect.setDataType("varchar");
        collect.setDataLength(200);
        collect.setDataScale(2);
        collect.setNullableYn(1);
        collect.setDefaultValue("new_default");
        collect.setIdentityYn(1);
        collect.setIdentityType("ALWAYS");
        collect.setColumnComment("변경 후");

        WorkDiffResult result = compare(
                snapshot(SnapshotType.COLLECT, List.of(collectTable), List.of(collect)),
                snapshot(SnapshotType.CURRENT, List.of(currentTable), List.of(current))
        );

        List<WorkChange> changes = component(target(result, "member"), "name").getChanges();
        Set<String> changedColumns = changes.stream()
                .map(WorkChange::getChangeColumn)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                ChangeColumn.COLUMN_ORDER.name(),
                ChangeColumn.DATA_TYPE.name(),
                ChangeColumn.DATA_LENGTH.name(),
                ChangeColumn.DATA_SCALE.name(),
                ChangeColumn.NULLABLE_YN.name(),
                ChangeColumn.DEFAULT_VALUE.name(),
                ChangeColumn.IDENTITY_YN.name(),
                ChangeColumn.IDENTITY_TYPE.name(),
                ChangeColumn.COLUMN_COMMENT.name()
        ), changedColumns);
    }

    @Test
    void 동일한_테이블명이_중복되면_비교하지_않는다() {
        TableSnapshot first = table(1L, "member", "회원1");
        TableSnapshot second = table(2L, "member", "회원2");

        assertThrows(
                IllegalStateException.class,
                () -> compare(
                        snapshot(SnapshotType.COLLECT, List.of(first, second), List.of()),
                        snapshot(SnapshotType.CURRENT, List.of(), List.of())
                )
        );
    }

    @Test
    void 동일한_컬럼명이_중복되면_비교하지_않는다() {
        TableSnapshot collectTable = table(1L, "member", "회원");
        TableSnapshot currentTable = table(2L, "member", "회원");
        ColumnSnapshot first = column(11L, 1L, "member_id");
        ColumnSnapshot second = column(12L, 1L, "member_id");

        assertThrows(
                IllegalStateException.class,
                () -> compare(
                        snapshot(SnapshotType.COLLECT, List.of(collectTable), List.of(first, second)),
                        snapshot(SnapshotType.CURRENT, List.of(currentTable), List.of())
                )
        );
    }

    private WorkDiffResult compare(Snapshot collect, Snapshot current) {
        SnapshotService snapshotService = new StubSnapshotService(collect, current);
        return new CompareService(snapshotService).compare(DB_CONFIG_ID);
    }

    private Snapshot snapshot(SnapshotType type, List<TableSnapshot> tables, List<ColumnSnapshot> columns) {
        return new Snapshot(type, DB_CONFIG_ID, null, tables, columns);
    }

    private TableSnapshot table(Long id, String name, String comment) {
        TableSnapshot table = new TableSnapshot();
        table.setTableSnapshotId(id);
        table.setDbConfigId(DB_CONFIG_ID);
        table.setTableName(name);
        table.setTableComment(comment);
        table.setTableType("TABLE");
        return table;
    }

    private ColumnSnapshot column(Long id, Long tableId, String name) {
        ColumnSnapshot column = new ColumnSnapshot();
        column.setColumnSnapshotId(id);
        column.setTableSnapshotId(tableId);
        column.setColumnName(name);
        column.setColumnComment("컬럼");
        column.setColumnOrder(1);
        column.setDataType("bigint");
        column.setDataLength(64);
        column.setDataScale(0);
        column.setNullableYn(0);
        column.setDefaultValue(null);
        column.setIdentityYn(0);
        column.setIdentityType(null);
        return column;
    }

    private WorkTarget target(WorkDiffResult result, String tableName) {
        return result.getTargets().stream()
                .filter(target -> tableName.equals(target.getObjectName()))
                .findFirst()
                .orElseThrow();
    }

    private WorkComponent component(WorkTarget target, String columnName) {
        return target.getComponents().stream()
                .filter(component -> columnName.equals(component.getComponentName()))
                .findFirst()
                .orElseThrow();
    }

    private static class StubSnapshotService extends SnapshotService {
        private final Snapshot collectSnapshot;
        private final Snapshot currentSnapshot;

        private StubSnapshotService(Snapshot collectSnapshot, Snapshot currentSnapshot) {
            this.collectSnapshot = collectSnapshot;
            this.currentSnapshot = currentSnapshot;
        }

        @Override
        public Snapshot findCollectSnapshot(Long dbConfigId) {
            return collectSnapshot;
        }

        @Override
        public Snapshot findCurrentSnapshot(Long dbConfigId) {
            return currentSnapshot;
        }
    }
}
