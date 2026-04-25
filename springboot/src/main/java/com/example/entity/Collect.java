package com.example.entity;

public class Collect {
    private Integer id;
    private Integer userId;
    private Integer caseId;

    // 关联字段（用于展示）
    private String userName;
    private String caseTitle;
    private Case caseObj;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public Case getCaseObj() {
        return caseObj;
    }

    public void setCaseObj(Case caseObj) {
        this.caseObj = caseObj;
    }
}