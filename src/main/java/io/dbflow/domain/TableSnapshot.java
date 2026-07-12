package io.dbflow.domain;

import java.util.ArrayList;
import java.util.List;

public class TableSnapshot {
    private Long tableSnapshotId;
    private Long commitLogId;
    private Long dbConfigId;
    private String tableName;
    private String tableComment;
    private String tableType;
    private String ownerName;
    private Integer tableDeletedYn;
    private final List<ColumnSnapshot> columns = new ArrayList<>();

    public void addColumn(ColumnSnapshot column) {
        columns.add(column);
    }

    public Long getTableSnapshotId() {
        return tableSnapshotId;
    }

    public void setTableSnapshotId(Long tableSnapshotId) {
        this.tableSnapshotId = tableSnapshotId;
    }

    public Long getCommitLogId() {
        return commitLogId;
    }

    public void setCommitLogId(Long commitLogId) {
        this.commitLogId = commitLogId;
    }

    public Long getDbConfigId() {
        return dbConfigId;
    }

    public void setDbConfigId(Long dbConfigId) {
        this.dbConfigId = dbConfigId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getTableDeletedYn() {
        return tableDeletedYn;
    }

    public void setTableDeletedYn(Integer tableDeletedYn) {
        this.tableDeletedYn = tableDeletedYn;
    }

    public List<ColumnSnapshot> getColumns() {
        return columns;
    }
}
