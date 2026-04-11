package com.example.controller;

import com.example.common.Result;
import com.example.entity.AiChat;
import com.example.service.AiChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI反诈模拟对话控制层
 * 负责接收请求、参数校验、会话ID管理、调用服务层、统一响应封装
 */
//控制层：接受请求
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Resource
    private AiChatService aiChatService;

    @PostMapping("/chat") //处理POST请求
    public Result chat(@RequestBody AiChat aichat) {
        String sessionId = aichat.getSessionId();
        Map<String, Object> message=aiChatService.chat(aichat.getScene(), aichat.getMessage(), sessionId);
        return Result.success(message);
    }

    // 新增：开始新场景，AI主动说话
    @PostMapping("/start")
    public Result startScene(@RequestBody AiChat aichat) {
        System.out.println("接收到的 scene: " + aichat.getScene());
        System.out.println("接收到的 sessionId: " + aichat.getSessionId());
        System.out.println("接收到的 message: " + aichat.getMessage());
        // 获取会话ID
        String sessionId = aichat.getSessionId();
        Map<String, Object> openingLine =aiChatService.startScene(aichat.getScene(), sessionId);

        return Result.success(openingLine);
    }

}
