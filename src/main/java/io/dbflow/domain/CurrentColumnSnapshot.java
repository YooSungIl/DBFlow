package io.dbflow.domain;

public class CurrentColumnSnapshot {
    private Long currentColumnId;
    private Long currentTableId;

    private String tableName;
    private String tableComment;
    private String tableType;

    private String columnName;
    private String columnComment;
    private Integer columnOrder;
    private String dataType;
    private Integer dataLength;
    private Integer dataScale;
    private Integer nullableYn;
    private String defaultValue;
    private Integer identityYn;
    private String identityType;

    public Long getCurrentColumnId() {
        return currentColumnId;
    }

    public void setCurrentColumnId(Long currentColumnId) {
        this.currentColumnId = currentColumnId;
    }

    public Long getCurrentTableId() {
        return currentTableId;
    }

    public void setCurrentTableId(Long currentTableId) {
        this.currentTableId = currentTableId;
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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public Integer getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(Integer columnOrder) {
        this.columnOrder = columnOrder;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getDataLength() {
        return dataLength;
    }

    public void setDataLength(Integer dataLength) {
        this.dataLength = dataLength;
    }

    public Integer getDataScale() {
        return dataScale;
    }

    public void setDataScale(Integer dataScale) {
        this.dataScale = dataScale;
    }

    public Integer getNullableYn() {
        return nullableYn;
    }

    public void setNullableYn(Integer nullableYn) {
        this.nullableYn = nullableYn;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getIdentityYn() {
        return identityYn;
    }

    public void setIdentityYn(Integer identityYn) {
        this.identityYn = identityYn;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }
}
