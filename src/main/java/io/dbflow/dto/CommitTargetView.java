package io.dbflow.dto;

import java.util.List;

public class CommitTargetView {

    private Long commitTargetId;
    private String objectType;
    private String objectName;
    private String objectComment;
    private String changeType;

    private List<CommitChangeDetailView> changes;

    public Long getCommitTargetId() {
        return commitTargetId;
    }

    public void setCommitTargetId(Long commitTargetId) {
        this.commitTargetId = commitTargetId;
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

    public List<CommitChangeDetailView> getChanges() {
        return changes;
    }

    public void setChanges(List<CommitChangeDetailView> changes) {
        this.changes = changes;
    }
}
