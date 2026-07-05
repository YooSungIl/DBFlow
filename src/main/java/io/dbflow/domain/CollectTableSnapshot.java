package io.dbflow.domain;

public class CollectTableSnapshot {
    private Long collectTableId;
    private Long dbConfigId;
    private String tableName;
    private String tableComment;
    private String tableType;
    private String ownerName;

    public Long getCollectTableId() {
        return collectTableId;
    }

    public void setCollectTableId(Long collectTableId) {
        this.collectTableId = collectTableId;
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
}
