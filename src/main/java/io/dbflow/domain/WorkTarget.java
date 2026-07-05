package io.dbflow.domain;

import java.util.ArrayList;
import java.util.List;

public class WorkTarget {
    private Long workTargetId;
    private Long dbConfigId;
    private String objectType;
    private String objectName;
    private String objectComment;
    private String changeType;
    private String comparedAt;

    private final List<WorkComponent> components = new ArrayList<>();

    public WorkTarget(Long dbConfigId, String objectType, String objectName, String objectComment, String changeType) {
        this.dbConfigId = dbConfigId;
        this.objectType = objectType;
        this.objectName = objectName;
        this.objectComment = objectComment;
        this.changeType = changeType;
    }

    public List<WorkComponent> getComponents() {
        return components;
    }

    public WorkComponent addComponent(WorkComponent component) {
        components.add(component);
        return component;
    }

    public Long getWorkTargetId() {
        return workTargetId;
    }

    public void setWorkTargetId(Long workTargetId) {
        this.workTargetId = workTargetId;
    }

    public Long getDbConfigId() {
        return dbConfigId;
    }

    public void setDbConfigId(Long dbConfigId) {
        this.dbConfigId = dbConfigId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectComment() {
        return objectComment;
    }

    public void setObjectComment(String objectComment) {
        this.objectComment = objectComment;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getComparedAt() {
        return comparedAt;
    }

    public void setComparedAt(String comparedAt) {
        this.comparedAt = comparedAt;
    }
}
