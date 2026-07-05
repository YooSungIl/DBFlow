package io.dbflow.domain;

public class WorkChange {
    Long workChangeId;
    Long workComponentId;
    String changeColumn;
    String beforeValue;
    String afterValue;

    public WorkChange(String changeColumn, String beforeValue, String afterValue) {
        this.changeColumn = changeColumn;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    public Long getWorkChangeId() {
        return workChangeId;
    }

    public void setWorkChangeId(Long workChangeId) {
        this.workChangeId = workChangeId;
    }

    public Long getWorkComponentId() {
        return workComponentId;
    }

    public void setWorkComponentId(Long workComponentId) {
        this.workComponentId = workComponentId;
    }

    public String getChangeColumn() {
        return changeColumn;
    }

    public void setChangeColumn(String changeColumn) {
        this.changeColumn = changeColumn;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }
}
