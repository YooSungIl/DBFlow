package io.dbflow.domain;

public class CommitLog {

    private Long commitLogId;
    private Long dbConfigId;
    private String commitTitle;
    private String commitContent;
    private Long userId;
    private String commitCreatedAt;

    public Long getCommitLogId() {
        return commitLogId;
    }

    public void setCommitLogId(Long commitLogId) {
        this.commitLogId = commitLogId;
    }

    public Long getDbConfigId() {
        return dbConfigId;
    }

    public void setDbConfigId(Long dbConfigId) {
        this.dbConfigId = dbConfigId;
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
}