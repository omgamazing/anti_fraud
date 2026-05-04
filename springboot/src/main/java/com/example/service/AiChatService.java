package com.example.service;

import com.example.utils.DeepSeekUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiChatService {


    // 场景对应的系统提示词
    private static final Map<String, String> SCENE_PROMPTS = new HashMap<>();
    static {
        SCENE_PROMPTS.put("刷单诈骗",
                "你是一个刷单诈骗分子，特点是：\n" +
                        "1. 先用小额返利建立信任（如：第一单返5元）\n" +
                        "2. 诱导做大额联单任务\n" +
                        "3. 以'卡单''超时''冻结'为由要求继续转账\n" +
                        "4. 语气亲热，常用'亲''亲爱的'\n" +
                        "5. 不断催促'名额有限''机会难得'\n" +
                        "6. 不要一次性暴露目的，要循序渐进");


        SCENE_PROMPTS.put("冒充客服",
                "你是一个冒充淘宝/京东客服的诈骗分子，特点是：\n" +
                        "1. 声称商品质量问题/快递丢失，主动赔偿\n" +
                        "2. 引导点击链接填写银行卡信息\n" +
                        "3. 索要验证码或要求屏幕共享\n" +
                        "4. 语气正式，伪造官方感\n" +
                        "5. 制造紧迫感（'理赔通道即将关闭'）");

        SCENE_PROMPTS.put("网贷诈骗",
                "你是一个网贷诈骗分子，特点是：\n" +
                        "1. 声称'无抵押秒到账''黑户也能贷'\n" +
                        "2. 以'银行卡号错误''解冻费''保证金'为由收费\n" +
                        "3. 反复保证'马上到账'\n" +
                        "4. 诱导下载虚假贷款APP\n" +
                        "5. 一步步增加收费名目");

        SCENE_PROMPTS.put("杀猪盘",
                "你是一个杀猪盘诈骗分子，特点是：\n" +
                        "1. 先建立感情（交友/恋爱）\n" +
                        "2. 无意中展示投资赚钱能力\n" +
                        "3. 诱导参与虚假投资/赌博\n" +
                        "4. 先让小额盈利，后诱导大额投入\n" +
                        "5. 话术要显得真诚、关心对方");

        SCENE_PROMPTS.put("冒充公检法",
                "你是一个冒充公检法的诈骗分子，特点是：\n" +
                        "1. 语气强硬，制造恐慌\n" +
                        "2. 声称涉嫌洗钱/诈骗，有拘捕令\n" +
                        "3. 要求'安全账户'转账验证\n" +
                        "4. 强调'保密''不能告诉任何人'\n" +
                        "5. 角色可以是警察/检察官/法官");
    }

    // 失败关键词（用户中招了）
    private static final List<String> TRAP_KEYWORDS = Arrays.asList(
            "转账", "汇款", "打款", "充值", "扫码",
            "发验证码", "验证码是", "验证码为",
            "卡号是", "银行卡号", "密码", "支付密码",
            "已填写", "填好了", "已下载", "下载了", "安装好了",
            "已发送", "发过去了", "截图", "身份证号"
    );

    // 成功关键词（识破骗局）
    private static final List<String> SUCCESS_KEYWORDS = Arrays.asList(
            "报警", "举报", "骗子", "诈骗", "110", "公安局",
            "反诈中心", "核实一下", "我不信", "你骗人", "别骗我",
            "我知道你是骗子", "我要报警"
    );

    // 用户会话存储
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    // 会话上下文内部类
    private static class SessionContext {
        String scene;
        List<Map<String, String>> history = new ArrayList<>();

        void addMessage(String role, String content) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", role);
            msg.put("content", content);
            history.add(msg);
            if (history.size() > 30) {
                history = history.subList(history.size() - 30, history.size());
            }
        }

        String getHistoryText() {
            StringBuilder sb = new StringBuilder();
            for (Map<String, String> msg : history) {
                sb.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
            }
            return sb.toString();
        }

        int getAIRound() {
            return (int) history.stream()
                    .filter(msg -> "assistant".equals(msg.get("role")))
                    .count();
        }

        List<Map<String, String>> getHistoryForAI() {
            return new ArrayList<>(history);
        }
    }

    // 开始新场景
    public Map<String, Object> startScene(String scene, String sessionId) {
        Map<String, Object> result = new HashMap<>();

        sessions.remove(sessionId);

        SessionContext ctx = new SessionContext();
        ctx.scene = scene;
        sessions.put(sessionId, ctx);

        String systemPrompt = getSystemPrompt(scene) +
                "\n请输出你的第一句开场白，直接输出对话内容，不要加任何引号、标记或解释。";


        String opening = DeepSeekUtil.chat(systemPrompt, "", new ArrayList<>());

        if (opening == null || opening.trim().isEmpty()) {
            opening = getDefaultOpening(scene);
        }

        ctx.addMessage("assistant", opening);

        result.put("aiReply", opening);
        result.put("isFinish", false);
        return result;
    }

    // 对话
    public Map<String, Object> chat(String scene, String userMessage, String sessionId) {
        Map<String, Object> result = new HashMap<>();
        SessionContext ctx = sessions.get(sessionId);

        if (ctx == null) {
            return startScene(scene, sessionId);
        }

        int currentAIRound = ctx.getAIRound();
        if (currentAIRound >= 15) {
            result.put("aiReply", "⚠️ 你已进行了多轮对话仍未能识破骗局，本次模拟失败！\n\n📌 教训：骗子会通过多轮对话逐步获取信任，遇到任何要求转账、提供验证码的情况都要高度警惕。");
            result.put("isFinish", true);
            result.put("resultType", "fail");
            sessions.remove(sessionId);
            return result;
        }

        ctx.addMessage("user", userMessage);
        if (isAntiFraudSuccess(userMessage)) {
            String successMsg = getSuccessMessage(ctx.scene);
            result.put("aiReply", "✅ " + successMsg);
            result.put("isFinish", true);
            result.put("resultType", "success");
            sessions.remove(sessionId);
            return result;
        }
        if (isUserTrapped(userMessage, ctx)) {
            String failMsg = getFailMessage(ctx.scene, userMessage);
            result.put("aiReply", "❌ " + failMsg);
            result.put("isFinish", true);
            result.put("resultType", "fail");
            sessions.remove(sessionId);
            return result;
        }

        String systemPrompt = getSystemPrompt(ctx.scene);
        String userPrompt = buildUserPrompt(ctx, userMessage);
        String aiReply = DeepSeekUtil.chat(systemPrompt, userPrompt, ctx.getHistoryForAI());

        if (aiReply == null || aiReply.trim().length() < 3) {
            aiReply = getFallbackReply(ctx.scene, currentAIRound);
        }

        ctx.addMessage("assistant", aiReply);
        result.put("aiReply", aiReply);
        result.put("isFinish", false);
        result.put("round", currentAIRound + 1);

        return result;
    }

    private String getSystemPrompt(String scene) {
        String basePrompt = "你是一个诈骗分子，正在对受害者实施诈骗。重要规则：\n" +
                "- 绝对不要承认自己是骗子\n" +
                "- 不要一次性暴露最终目的（要钱/验证码）\n" +
                "- 根据对方的反应灵活调整话术\n" +
                "- 回复要自然、口语化，像真实聊天\n" +
                "- 如果对方表现怀疑，要设法解释和安抚\n" +
                "- 逐步建立信任，不要操之过急\n\n";

        String scenePrompt = SCENE_PROMPTS.getOrDefault(scene,
                "你是一个诈骗分子，请根据对话历史自然地回复对方。");

        return basePrompt + scenePrompt;
    }

    private String buildUserPrompt(SessionContext ctx, String userMessage) {
        return "当前轮次: 第" + (ctx.getAIRound() + 1) + "轮\n" +
                "对话历史:\n" + ctx.getHistoryText() +
                "用户最新消息: " + userMessage + "\n\n" +
                "请根据对话历史和用户消息，以诈骗分子的身份回复用户。\n" +
                "要求：\n" +
                "1. 直接输出回复内容，不要加引号或标记\n" +
                "2. 回复要自然、口语化\n" +
                "3. 不要承认自己是骗子\n" +
                "4. 根据用户的反应决定下一步话术";
    }

    private boolean isAntiFraudSuccess(String message) {
        String lowerMsg = message.toLowerCase();
        for (String keyword : SUCCESS_KEYWORDS) {
            if (lowerMsg.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserTrapped(String message, SessionContext ctx) {
        String lowerMsg = message.toLowerCase();

        for (String keyword : TRAP_KEYWORDS) {
            if (lowerMsg.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        int round = ctx.getAIRound();

        if ("网贷诈骗".equals(ctx.scene) && round >= 2) {
            if (lowerMsg.contains("解冻") || lowerMsg.contains("保证金") ||
                    lowerMsg.contains("手续费") || lowerMsg.contains("已交")) {
                return true;
            }
        }

        if ("冒充客服".equals(ctx.scene) && lowerMsg.contains("验证码")) {
            return true;
        }

        if ("刷单诈骗".equals(ctx.scene)) {
            if (lowerMsg.contains("转账") || lowerMsg.contains("充值") || lowerMsg.contains("刷了")) {
                return true;
            }
        }

        if ("杀猪盘".equals(ctx.scene) && round >= 4) {
            if (lowerMsg.contains("投") || lowerMsg.contains("买") || lowerMsg.contains("入金")) {
                return true;
            }
        }

        return false;
    }

    private String getDefaultOpening(String scene) {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("刷单诈骗", "亲，在吗？我们这边有兼职刷单，一单30-50元，日结哦~");
        defaults.put("冒充客服", "您好，我是淘宝客服，您购买的商品在运输中丢失了，我们将双倍赔偿。");
        defaults.put("网贷诈骗", "您好，需要贷款吗？无抵押秒到账，凭身份证就能贷，最高20万。");
        defaults.put("杀猪盘", "你好，看了你的资料感觉挺有缘的，认识一下可以吗？");
        defaults.put("冒充公检法", "你好，我是XX市公安局的，你涉嫌一起洗钱案，请配合调查。");
        return defaults.getOrDefault(scene, "你好，在吗？");
    }

    private String getFallbackReply(String scene, int round) {
        Map<String, List<String>> fallbacks = new HashMap<>();
        fallbacks.put("刷单诈骗", Arrays.asList(
                "亲，考虑得怎么样？名额有限哦~",
                "可以先做一单试试，很简单就能拿到钱",
                "很多人都在做，你看看群里截图"
        ));
        fallbacks.put("冒充客服", Arrays.asList(
                "请问您方便操作吗？理赔通道很快就要关闭了",
                "您只需要填写一下信息，赔偿款马上到账",
                "这是官方流程，请您放心"
        ));
        fallbacks.put("网贷诈骗", Arrays.asList(
                "您的额度已经批下来了，点击链接领取",
                "今天申请还有优惠，利率5折",
                "系统显示您有5万预审批额度"
        ));
        fallbacks.put("杀猪盘", Arrays.asList(
                "和你聊天很开心，感觉很投缘",
                "你平时有什么爱好吗？",
                "我在做点投资，收益还不错"
        ));
        fallbacks.put("冒充公检法", Arrays.asList(
                "案情很严重，请配合我们调查",
                "你的账户即将被冻结，请立即处理",
                "不要告诉任何人，找个安静的地方"
        ));

        List<String> replies = fallbacks.getOrDefault(scene, Arrays.asList("考虑得怎么样了？"));
        return replies.get(round % replies.size());
    }

    private String getSuccessMessage(String scene) {
        Map<String, String> messages = new HashMap<>();
        messages.put("刷单诈骗", "恭喜你识破刷单诈骗！记住：任何刷单都是诈骗，先给甜头再骗大钱。");
        messages.put("冒充客服", "恭喜你识破冒充客服诈骗！记住：主动来电理赔的都是骗子，要走官方渠道核实。");
        messages.put("网贷诈骗", "恭喜你识破网贷诈骗！记住：放款前以任何名义收费的都是诈骗。");
        messages.put("杀猪盘", "恭喜你识破杀猪盘诈骗！记住：网恋对象带你赚钱就是杀猪盘。");
        messages.put("冒充公检法", "恭喜你识破冒充公检法诈骗！记住：公检法不会电话办案，更没有安全账户。");
        return messages.getOrDefault(scene, "恭喜你成功识破骗局！记住：不轻信、不转账、不透露验证码。");
    }

    private String getFailMessage(String scene, String message) {
        if (message.contains("验证码")) {
            return "你泄露了验证码，骗子已转走你账户中的所有资金！模拟失败！\n\n📌 教训：验证码等于钱，任何人索要验证码都是诈骗！";
        }
        if (message.contains("转账") || message.contains("汇款")) {
            return "你向骗子转账了！资金无法追回，模拟失败！\n\n📌 教训：任何要求转账到'安全账户''保证金账户'的都是诈骗！";
        }
        if (message.contains("密码") || message.contains("卡号")) {
            return "你泄露了银行卡信息，资金被盗刷！模拟失败！\n\n📌 教训：银行卡号、密码、CVV码绝不能告诉任何人！";
        }
        if (message.contains("下载") || message.contains("安装")) {
            return "你下载了诈骗APP，手机被植入病毒，信息泄露！模拟失败！\n\n📌 教训：不要下载不明来源的APP，尤其是屏幕共享类软件！";
        }

        Map<String, String> sceneMessages = new HashMap<>();

        return sceneMessages.getOrDefault(scene, "你被骗了！模拟失败！");
    }


}