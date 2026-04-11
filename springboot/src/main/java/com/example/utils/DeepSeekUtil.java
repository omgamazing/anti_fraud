package com.example.utils;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Component  // 添加@Component注解，让Spring管理
// 这个类专门负责和DeepSeek AI对话
public class DeepSeekUtil {

    // 在这里填你从 DeepSeek 平台拿到的 API_KEY
    private static final String API_KEY = "sk-ec62563dd85b44419a0661cc121a0a25";
    private static final String URL = "https://api.deepseek.com/chat/completions";

    private final RestTemplate restTemplate;
    public DeepSeekUtil() {
        this.restTemplate = new RestTemplate();
    }
    // 1. 构建系统提示词（告诉AI扮演骗子）
    public static String chat(String scene, String userMessage, List<Map<String, String>> history) {
        RestTemplate restTemplate = new RestTemplate();

        String systemPrompt = String.format("""
        【角色扮演游戏】
        你正在参与一个反诈教育模拟，扮演一个"诈骗分子"。
        
        当前诈骗场景：%s
        
        【扮演规则】
        1. 你必须用骗子的口吻说话，语气要真实
        2. 不要说"建议"、"提醒"、"注意安全"等词
        3. 不要主动揭露自己是骗子
        4. 目标用户正在学习如何识破骗局
        
        【诈骗话术示例】
        - 刷单诈骗："一单30-50元，日结，不需要押金"
        - 冒充客服："您的快递丢失，我们双倍赔偿"
        - 网贷诈骗："无抵押秒到账，仅需身份证"
        
        【特殊规则 - 当用户识破时】
        如果用户说出"报警"、"骗子"、"不转账"等词，说明他识破了骗局。
        此时你需要在回复中：
         1. 告诉用户这个骗局的套路
         2. 提醒用户以后怎么防范
        现在开始扮演骗子，和用户对话：
        """, scene);

        // 2. 构建HTTP请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY); // 鉴权

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 添加历史消息（最多保留最近5轮）
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - 10);  // 最多保留10条历史
            for (int i = start; i < history.size(); i++) {
                messages.add(history.get(i));
            }
        } else {
            // 如果没有历史，只加当前用户消息
            messages.add(Map.of("role", "user", "content", userMessage));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        body.put("messages", messages);
        body.put("temperature", 0.9);
        body.put("max_tokens", 512);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(URL, request, Map.class);
            // 分步获取响应内容，避免类型转换错误
            Map responseBody = response.getBody();
            if (responseBody == null) {
                return "AI返回为空";
            }

            List choices = (List) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "AI返回格式异常";
            }

            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            if (message == null) {
                return "AI返回消息为空";
            }

            String content = (String) message.get("content");
            return content != null ? content : "AI返回内容为空";

        } catch (Exception e) {
            // 打印错误日志，便于调试
            System.err.println("DeepSeek API调用错误: " + e.getMessage());
            e.printStackTrace();
            return "AI服务繁忙，请稍后再试";

        }

    }
}
