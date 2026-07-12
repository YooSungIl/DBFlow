package io.dbflow.domain;

import io.dbflow.common.enums.SnapshotType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Snapshot {
    private final SnapshotType snapshotType;
    private final Long dbConfigId;
    private final Long commitLogId;
    private final List<TableSnapshot> tables;

    public Snapshot(
            SnapshotType snapshotType,
            Long dbConfigId,
            Long commitLogId,
            List<TableSnapshot> tables,
            List<ColumnSnapshot> columns
    ) {
        this.snapshotType = snapshotType;
        this.dbConfigId = dbConfigId;
        this.commitLogId = commitLogId;
        this.tables = tables;
        attachColumns(tables, columns);
    }

    private void attachColumns(List<TableSnapshot> tables, List<ColumnSnapshot> columns) {
        Map<Long, TableSnapshot> tableMap = new LinkedHashMap<>();

        for (TableSnapshot table : tables) {
            TableSnapshot duplicate = tableMap.put(table.getTableSnapshotId(), table);
            if (duplicate != null) {
                throw new IllegalStateException("중복된 테이블 스냅샷 ID가 있습니다. tableSnapshotId=" + table.getTableSnapshotId());
            }
        }

        for (ColumnSnapshot column : columns) {
            TableSnapshot table = tableMap.get(column.getTableSnapshotId());
            if (table == null) {
                throw new IllegalStateException("컬럼에 매칭되는 테이블 스냅샷이 없습니다. tableSnapshotId=" + column.getTableSnapshotId());
            }
            table.addColumn(column);
        }
    }

    public SnapshotType getSnapshotType() {
        return snapshotType;
    }

    public Long getDbConfigId() {
        return dbConfigId;
    }

    public Long getCommitLogId() {
        return commitLogId;
    }

    public List<TableSnapshot> getTables() {
        return tables;
    }
}
