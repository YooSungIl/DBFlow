package io.dbflow.domain;

public class User {

    private Long userId;
    private Long dbConfigId;
    private String userName;
    private String userEmail;
    private Integer useYn;
    private String createdAt;
    private String updatedAt;

    public User() {
    }

    public User(String userName, String userEmail, Integer useYn, String createdAt, String updatedAt) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.useYn = useYn;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Integer getUseYn() {
        return useYn;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUseYn(Integer useYn) {
        this.useYn = useYn;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getDbConfigId() {
        return dbConfigId;
    }

    public void setDbConfigId(Long dbConfigId) {
        this.dbConfigId = dbConfigId;
    }
}