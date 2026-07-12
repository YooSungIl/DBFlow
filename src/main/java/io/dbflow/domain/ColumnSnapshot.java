package io.dbflow.domain;

public class ColumnSnapshot {
    private Long columnSnapshotId;
    private Long tableSnapshotId;
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

    public Long getColumnSnapshotId() {
        return columnSnapshotId;
    }

    public void setColumnSnapshotId(Long columnSnapshotId) {
        this.columnSnapshotId = columnSnapshotId;
    }

    public Long getTableSnapshotId() {
        return tableSnapshotId;
    }

    public void setTableSnapshotId(Long tableSnapshotId) {
        this.tableSnapshotId = tableSnapshotId;
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
