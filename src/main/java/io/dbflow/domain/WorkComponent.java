package io.dbflow.domain;

import java.util.ArrayList;
import java.util.List;

public class WorkComponent {
    private Long workComponentId;
    private Long workTargetId;
    private String componentType;
    private String componentName;
    private String componentComment;
    private String changeType;

    private final List<WorkChange> changes = new ArrayList<>();

    public WorkComponent(String componentType, String componentName, String componentComment, String changeType) {
        this.componentType = componentType;
        this.componentName = componentName;
        this.componentComment = componentComment;
        this.changeType = changeType;
    }

    public List<WorkChange> getChanges() {
        return changes;
    }

    public void addChange(WorkChange change) {
        changes.add(change);
    }

    public Long getWorkComponentId() {
        return workComponentId;
    }

    public void setWorkComponentId(Long workComponentId) {
        this.workComponentId = workComponentId;
    }

    public Long getWorkTargetId() {
        return workTargetId;
    }

    public void setWorkTargetId(Long workTargetId) {
        this.workTargetId = workTargetId;
    }

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
}
