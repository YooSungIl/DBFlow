package io.dbflow.dto;

import java.util.List;

public class CommitLogView {

    private Long commitLogId;
    private String dbAlias;
    private String dbName;
    private String commitTitle;
    private String commitContent;
    private Long userId;
    private String commitCreatedAt;

    private List<CommitTargetView> targets;

    public Long getCommitLogId() {
        return commitLogId;
    }

    public void setCommitLogId(Long commitLogId) {
        this.commitLogId = commitLogId;
    }

    public String getDbAlias() {
        return dbAlias;
    }

    public void setDbAlias(String dbAlias) {
        this.dbAlias = dbAlias;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getCommitTitle() {
        return commitTitle;
    }

    public void setCommitTitle(String commitTitle) {
        this.commitTitle = commitTitle;
    }

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCommitCreatedAt() {
        return commitCreatedAt;
    }

    public void setCommitCreatedAt(String commitCreatedAt) {
        this.commitCreatedAt = commitCreatedAt;
    }

    public List<CommitTargetView> getTargets() {
        return targets;
    }

    public void setTargets(List<CommitTargetView> targets) {
        this.targets = targets;
    }
}
