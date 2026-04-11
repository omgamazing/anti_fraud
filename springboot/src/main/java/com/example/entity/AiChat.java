package com.example.entity;

//@Data
public class AiChat{
    private String scene;     // 诈骗场景（如：刷单诈骗）
    private String message;    // 用户输入的消息
    private String sessionId;    // 会话ID（新增）

    // 这个类就像"信封"，用来装前端传来的数据
    // 当用户在前端输入内容，Spring Boot会自动把JSON数据转成这个对象


    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


}
