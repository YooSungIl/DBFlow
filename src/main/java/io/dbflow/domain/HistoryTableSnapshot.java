package io.dbflow.domain;

public class HistoryTableSnapshot {
    private Long historyTableId;
    private Long commitLogId;
    private Long dbConfigId;
    private String tableName;
    private String tableComment;
    private String tableType;
    private String ownerName;
    private Integer tableDeleteYn;

    public Integer getTableDeleteYn() {
        return tableDeleteYn;
    }

    public void setTableDeleteYn(Integer tableDeleteYn) {
        this.tableDeleteYn = tableDeleteYn;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getDbConfigId() {
        return dbConfigId;
    }

    public void setDbConfigId(Long dbConfigId) {
        this.dbConfigId = dbConfigId;
    }

    public Long getCommitLogId() {
        return commitLogId;
    }

    public void setCommitLogId(Long commitLogId) {
        this.commitLogId = commitLogId;
    }

    public Long getHistoryTableId() {
        return historyTableId;
    }

    public void setHistoryTableId(Long historyTableId) {
        this.historyTableId = historyTableId;
    }
}
