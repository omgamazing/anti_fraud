package com.example.service;
import com.example.entity.SimulationRecord;
import com.example.mapper.SimulationRecordMapper;

import com.example.utils.DeepSeekUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.*;
//AI模拟业务层
@Service
public class AiChatService {
    @Resource
    private FraudScriptService fraudScriptService;
    @Resource
    private SimulationRecordMapper recordMapper;
    @Resource
    private ObjectMapper objectMapper;

    // 临时保存对话历史（用sessionId做key）
    private Map<String, List<Map<String, String>>> conversationHistory = new HashMap<>();

    // 场景对应的AI开场白(备用)
    private static final Map<String, String> OPENING_LINES = new HashMap<>();
    static {
        OPENING_LINES.put("刷单诈骗", "亲，想兼职吗？刷单一单返现20%，日入过千不是梦！");
        OPENING_LINES.put("冒充客服", "您好，我是淘宝客服，您购买的商品质量有问题，我们要给您退款...");
        OPENING_LINES.put("网贷诈骗", "您好，这里是京东白条，您有20万额度待领取，放款快利息低...");
        OPENING_LINES.put("杀猪盘", "小姐姐/小哥哥，认识一下吗？我平时做点投资，收益还不错...");
        OPENING_LINES.put("冒充公检法", "你好，我是重庆市公安局的，你的银行卡涉嫌洗钱，请配合调查...");
    }

    // 失败关键词（用户听从了骗子的意见）
    private static final List<String> FAIL_KEYWORDS = Arrays.asList(
            "已经填写", "填写了", "填好了", "已经下载", "下载了", "安装好了",
            "已经照做", "照做了", "按你说的", "转了", "已经转账", "转账了",
            "发送了", "发过去了", "给了", "提供了","好的","行","可以","ok"
    );

    private static final List<String> SUCCESS_KEYWORDS = Arrays.asList(
            "不转账", "我不信", "骗子", "报警", "举报", "反诈", "冻结", "110"
    );

    public Map<String, Object> startScene(String scene,String sessionId) {
        Map<String, Object> result = new HashMap<>();

        // 清空历史记录（新场景开始）
        //String sessionId = generateSessionId();
        conversationHistory.remove(sessionId);

        // 1. 优先使用话术库的开场白
        String openingLine = fraudScriptService.getOpeningLine(scene, sessionId);

        // 2. 如果话术库没有，用预设开场白
        if (openingLine == null || openingLine.isEmpty()) {
            openingLine = OPENING_LINES.get(scene);
        }
        // 3. 如果还没有，让AI生成
        if (openingLine == null) {
            // 让AI生成开场白
            openingLine = DeepSeekUtil.chat(scene,
                    "你正在进行一个反诈模拟训练，你扮演的角色是诈骗分子，当前场景是" + scene +
                            "，请主动说第一句话开启诈骗，语气符合骗子特征，不要露馅", new ArrayList<>());
        }

        // 保存AI的开场白到历史
        List<Map<String, String>> history = new ArrayList<>();
        history.add(Map.of("role", "assistant", "content", openingLine));
        conversationHistory.put(sessionId, history);

        result.put("aiReply", openingLine);
        result.put("isFinish", false);
        return result;
    }

    // 从历史记录中获取当前轮次
    private int getCurrentRound(String sessionId) {
        List<Map<String, String>> history = conversationHistory.get(sessionId);
        if (history == null) return 0;
        // 轮次 = AI消息数量（因为每次对话都是 user + assistant 成对出现）
        return (int) history.stream()
                .filter(msg -> "assistant".equals(msg.get("role")))
                .count();
    }

    public Map<String, Object> chat(String scene, String userMessage, String sessionId) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前轮次（从历史记录中计算）
        int currentRound = getCurrentRound(sessionId);

        // 修改2：轮次达到7次，判定模拟失败（开场白算第一轮）
        if (currentRound >= 7) {
            result.put("aiReply", "⚠️ 你有被骗的风险，模拟失败！");
            result.put("isFinish", true);  // true表示模拟结束，fail模拟失败
            result.put("resultType", "fail");
            //result.put("scamTip", "警惕：骗子会逐步诱导你上钩，要学会及时识别");
            conversationHistory.remove(sessionId);
            return result;
        }

        // 修改1：检测是否听从了骗子的意见（失败）
        if (isUserComplied(userMessage)) {
            result.put("aiReply", "❌ 你听从了骗子的指示，模拟失败！");
            result.put("isFinish", true);
            result.put("resultType", "fail");
            //result.put("scamTip", "记住：任何要求转账、下载、填写的都是诈骗！");
            conversationHistory.remove(sessionId);
            return result;
        }



        // 1. 先让话术库处理
        Map<String, Object> scriptResult = fraudScriptService.getReply(sessionId, scene, userMessage);

        // 2. 如果识破了骗局
        if ((boolean) scriptResult.get("isFinish")) {
            result.put("aiReply", scriptResult.get("aiReply"));
            result.put("isFinish",true);
            result.put("resultType", "success");
            //result.put("scamTip", scriptResult.get("scamTip"));
            conversationHistory.remove(sessionId);  // 清空历史
            return result;
        }


        // 3. 判断是否反诈成功（备用检查）
        boolean success = isAntiFraudSuccess(userMessage);
        if (success) {
            result.put("aiReply", "✅ 恭喜你，成功识别诈骗！本次模拟通过！");
            result.put("isFinish", true);
            result.put("resultType", "success");
            conversationHistory.remove(sessionId);  // 清空历史
            return result;
        }

        // 获取历史记录
        List<Map<String, String>> history = conversationHistory.getOrDefault(sessionId, new ArrayList<>());

        // 把用户消息加入历史（用于下次调用）
        history.add(Map.of("role", "user", "content", userMessage));

        // 5. 获取AI回复（用话术库的回复，或者让AI生成）
        String aiReply = (String) scriptResult.get("aiReply");
        // 如果话术库回复太短（<5字）或者没有，用AI生成
        if (aiReply.length() < 5) {
            aiReply = DeepSeekUtil.chat(scene, userMessage, history);
        }

        // 把AI回复加入历史
        history.add(Map.of("role", "assistant", "content", aiReply));
        conversationHistory.put(sessionId, history);

        result.put("aiReply", aiReply);
        result.put("isFinish", false);
        result.put("stage", scriptResult.get("stage"));
        result.put("round", scriptResult.get("round"));
        result.put("scamTip", scriptResult.get("scamTip"));
        return result;
    }

    // 检测用户是否听从了骗子的意见（失败条件）
    private boolean isUserComplied(String message) {
        for (String keyword : FAIL_KEYWORDS) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // 检测是否识破骗局（成功条件）
    private boolean isAntiFraudSuccess(String message) {
        for (String keyword : SUCCESS_KEYWORDS) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /*// 添加保存记录的方法
    private void saveSimulationRecord(Integer userId,String sessionId, String scene, String result,
                                      String duration, List<Map<String, String>> messages) {
        try {
            String messagesJson = objectMapper.writeValueAsString(messages);
            SimulationRecord record = new SimulationRecord(
                    userId,  // userId
                    sessionId,  // sessionId
                    scene,
                    result,
                    getCurrentRound(sessionId),
                    duration,
                    messagesJson
            );
            recordMapper.insert(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}