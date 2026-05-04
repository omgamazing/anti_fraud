package com.example.utils;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

// 这个类专门负责和DeepSeek AI对话
// 添加@Component注解，让Spring管理
@Component
public class DeepSeekUtil {

    // DeepSeek 平台拿到的 API_KEY
    private static final String API_KEY = "sk-ec62563dd85b44419a0661cc121a0a25";
    private static final String URL = "https://api.deepseek.com/chat/completions";

    private final RestTemplate restTemplate;

    public DeepSeekUtil() {
        this.restTemplate = new RestTemplate();
    }

    public static String chat(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                messages.add(history.get(i));
            }
        }
        if (userMessage != null && !userMessage.isEmpty()) {
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
            Map responseBody = response.getBody();
            if (responseBody == null) {
                return "";
            }

            List choices = (List) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "";
            }

            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            if (message == null) {
                return "";
            }

            String content = (String) message.get("content");
            return content != null ? content : "";

        } catch (Exception e) {
            System.err.println("DeepSeek API调用错误: " + e.getMessage());
            return "";
        }
    }
}
