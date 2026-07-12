package io.dbflow.dto;

public class CommitChangeDetailView {

    private String componentType;
    private String componentName;
    private String componentComment;
    private String changeType;
    private String changeColumn;
    private String beforeValue;
    private String afterValue;

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentComment() {
        return componentComment;
    }

    public void setComponentComment(String componentComment) {
        this.componentComment = componentComment;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
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
