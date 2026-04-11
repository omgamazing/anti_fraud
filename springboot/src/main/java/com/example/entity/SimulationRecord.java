package com.example.entity;

import java.time.LocalDateTime;

public class SimulationRecord {

    private Integer id;
    private Integer userId;
    private String sessionId;
    private String scene;
    private String result;
    private Integer rounds;
    private String duration;
    private String messages;
    private LocalDateTime createTime;


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Integer getRounds() { return rounds; }
    public void setRounds(Integer rounds) { this.rounds = rounds; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getMessages() { return messages; }
    public void setMessages(String messages) { this.messages = messages; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}