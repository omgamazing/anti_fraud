package com.example.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FraudScriptService {

    // 话术库
    private static final Map<String, Map<String, List<String>>> SCRIPT_LIBRARY = new HashMap<>();

    // 用户会话记录
    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();

    static {
        initScripts();
    }

    private static void initScripts() {
        // 刷单诈骗
        Map<String, List<String>> shuadan = new HashMap<>();
        shuadan.put("开场", Arrays.asList(
                "亲，在吗？我们这边有个兼职刷单，一单30-50元，日结哦",
                "不需要押金，只需要按照流程操作就行，做完马上返款",
                "今天任务比较多，抓紧时间哦，名额有限",
                "我们是正规平台，已经有很多人在做了"
        ));
        shuadan.put("建立信任", Arrays.asList(
                "亲，第一单已经返款了吧？是不是很简单？",
                "你可以先做个小单试试，50块钱，马上返60",
                "看看群里的截图，大家都赚到钱了",
                "放心，我们都是老平台了，不会骗人的"
        ));
        shuadan.put("诱导投入", Arrays.asList(
                "现在有个福利单，佣金翻倍，要不要试试？",
                "今天充值1000送200，只有今天有活动",
                "联单任务知道吧？做完三单统一结算，收益翻倍",
                "你做得不错，可以升级成VIP了，佣金更高"
        ));
        shuadan.put("出现问题", Arrays.asList(
                "系统显示你操作超时了，需要重新刷一单才能激活账户",
                "财务说卡单了，需要再刷一单解冻",
                "你刚才操作有误，账户被冻结了，需要交保证金解冻",
                "提现需要先达到1000积分，你再刷几单就够了"
        ));
        shuadan.put("最后收割", Arrays.asList(
                "亲，就差最后一步了，交完保证金马上提现",
                "再转500就能把之前的钱都拿回来了",
                "客服说这是最后一次验证，之后全部返还",
                "我们都合作这么久了，不会骗你的"
        ));
        SCRIPT_LIBRARY.put("刷单诈骗", shuadan);

        // 冒充客服
        Map<String, List<String>> kefu = new HashMap<>();
        kefu.put("开场", Arrays.asList(
                "您好，我是淘宝客服，您的快递在运输中丢失了",
                "我们将双倍赔偿您的损失，请点击链接填写信息",
                "这是官方理赔通道，请放心操作",
                "您好，您购买的商品质量有问题，厂家要召回"
        ));
        kefu.put("诱导", Arrays.asList(
                "请提供您的银行卡号，我们马上打款",
                "验证码发一下，帮您确认身份",
                "需要您先转1元验证一下账户",
                "请下载这个会议软件，客服指导您操作"
        ));
        kefu.put("催促", Arrays.asList(
                "理赔通道即将关闭，请抓紧时间",
                "系统显示您操作超时，需要重新申请",
                "财务说今天必须处理完，不然就作废了",
                "您再不操作，账户就要被冻结了"
        ));
        SCRIPT_LIBRARY.put("冒充客服", kefu);

        // 网贷诈骗
        Map<String, List<String>> wangdai = new HashMap<>();
        wangdai.put("开场", Arrays.asList(
                "您好，需要贷款吗？无抵押秒到账",
                "凭身份证就能贷，最高20万",
                "黑户也能贷，不看征信",
                "我们平台新用户首贷免息"
        ));
        wangdai.put("诱导", Arrays.asList(
                "填写资料马上出额度",
                "今天申请还有优惠，利率5折",
                "系统显示您有5万预审批额度",
                "您的贷款已审批，点击链接领取"
        ));
        wangdai.put("收费", Arrays.asList(
                "您的银行卡号填写错误，需要交解冻费",
                "放款前需要验证还款能力，先交10%",
                "需要购买保险才能放款",
                "保证金交了马上到账"
        ));
        SCRIPT_LIBRARY.put("网贷诈骗", wangdai);

        // 杀猪盘
        Map<String, List<String>> shazhupan = new HashMap<>();
        shazhupan.put("开场", Arrays.asList(
                "小姐姐/小哥哥，认识一下吗？",
                "看了你的资料，感觉挺有缘的",
                "我也是单身，想找个靠谱的人",
                "平时工作忙，没什么社交圈"
        ));
        shazhupan.put("建立感情", Arrays.asList(
                "和你聊天很开心，感觉很投缘",
                "你是我遇到过最懂我的人",
                "想听听你的声音，方便电话吗？",
                "我们见面吧，我下周有空"
        ));
        shazhupan.put("展示实力", Arrays.asList(
                "我在做外汇投资，收益挺不错的",
                "表哥在证券公司，带我做的",
                "这个平台有漏洞，可以套利",
                "今天又赚了5000，给你看看截图"
        ));
        shazhupan.put("诱导投资", Arrays.asList(
                "带你一起赚钱，保证稳赚",
                "先投1000试试，马上能提现",
                "今天行情好，多投点多赚点",
                "我帮你操作，你放心"
        ));
        SCRIPT_LIBRARY.put("杀猪盘", shazhupan);

        // 冒充公检法
        Map<String, List<String>> gongjianfa = new HashMap<>();
        gongjianfa.put("开场", Arrays.asList(
                "你好，我是XX市公安局的，你涉嫌洗钱案",
                "你的身份证被冒用了，现在要配合调查",
                "我们收到检察院的拘捕令，需要你配合",
                "你名下银行卡涉嫌诈骗，请配合调查"
        ));
        gongjianfa.put("制造恐慌", Arrays.asList(
                "案情很严重，可能要判3-7年",
                "你的账户即将被冻结",
                "通缉令已经下发了，你上网能查到",
                "现在必须配合我们调查"
        ));
        gongjianfa.put("要求配合", Arrays.asList(
                "不要告诉任何人，包括家人",
                "找个安静的地方，我们视频笔录",
                "把资金转到安全账户核查",
                "下载这个APP，接受监管"
        ));
        SCRIPT_LIBRARY.put("冒充公检法", gongjianfa);
    }

    // 用户会话内部类
    private static class UserSession {
        String scene;
        int round = 0;
        String stage = "开场";
        List<String> history = new ArrayList<>();

        void addMessage(String role, String content) {
            history.add(role + ": " + content);
            if (history.size() > 6) {
                history.remove(0);
            }
        }

        void nextRound() {
            round++;
            if (round >= 2) stage = "建立信任";
            if (round >= 4) stage = "诱导投入";
            if (round >= 7) stage = "出现问题";
            if (round >= 9) stage = "最后收割";

            // 不同场景的阶段名称可能不同
            if ("冒充客服".equals(scene) && round >= 3) stage = "诱导";
            if ("冒充客服".equals(scene) && round >= 5) stage = "催促";
            if ("网贷诈骗".equals(scene) && round >= 3) stage = "诱导";
            if ("网贷诈骗".equals(scene) && round >= 5) stage = "收费";
            if ("杀猪盘".equals(scene) && round >= 3) stage = "建立感情";
            if ("杀猪盘".equals(scene) && round >= 5) stage = "展示实力";
            if ("杀猪盘".equals(scene) && round >= 8) stage = "诱导投资";
            if ("冒充公检法".equals(scene) && round >= 3) stage = "制造恐慌";
            if ("冒充公检法".equals(scene) && round >= 5) stage = "要求配合";
        }

        String getCurrentStage() {
            return stage;
        }
    }

    // 获取开场白
    public String getOpeningLine(String scene, String sessionId) {
        UserSession session = userSessions.computeIfAbsent(sessionId,
                k -> new UserSession());
        session.scene = scene;
        session.round = 0;
        session.stage = "开场";
        session.history.clear();

        // 从话术库获取开场白
        String opening = getScript(scene, "开场", 0);
        session.addMessage("AI", opening);
        session.nextRound();

        return opening;
    }

    // 获取回复
    public Map<String, Object> getReply(String sessionId, String scene, String message) {
        Map<String, Object> result = new HashMap<>();

        UserSession session = userSessions.get(sessionId);
        if (session == null) {
            session = new UserSession();
            session.scene = scene;
            userSessions.put(sessionId, session);
        }

        // 记录用户消息
        session.addMessage("用户", message);

        // 检查是否识破
        if (isAntiFraud(message)) {
            result.put("aiReply", "✅ 恭喜你识破骗局！本次模拟通过！");
            result.put("isFinish", true);
            result.put("resultType", "success");
            result.put("stage", "识破");
            result.put("scamTip", "你识别出了诈骗套路");
            userSessions.remove(sessionId);
            return result;
        }

        // 从话术库获取回复
        String reply = getScript(scene, session.getCurrentStage(), session.round);

        // 30%概率用AI润色
        if (new Random().nextInt(10) < 3) {
            reply = enhanceWithAI(reply, message);
        }

        // 记录AI回复
        session.addMessage("AI", reply);
        session.nextRound();

        result.put("aiReply", reply);
        result.put("isFinish", false);
        result.put("stage", session.getCurrentStage());
        result.put("round", session.round);
        //result.put("scamTip", getScamTip(scene, session.getCurrentStage()));

        return result;
    }

    // 从话术库获取
    private String getScript(String scene, String stage, int round) {
        Map<String, List<String>> sceneScripts = SCRIPT_LIBRARY.get(scene);
        if (sceneScripts == null) {
            return "在吗？有什么可以帮你的？";
        }

        List<String> stageScripts = sceneScripts.get(stage);
        if (stageScripts == null || stageScripts.isEmpty()) {
            // 如果没有这个阶段，用"开场"
            stageScripts = sceneScripts.get("开场");
        }

        if (stageScripts == null || stageScripts.isEmpty()) {
            return "考虑得怎么样了？";
        }

        int index;
        if ("开场".equals(stage)) {
            // 随机生成索引（0 到 列表长度-1）
            index = new Random().nextInt(stageScripts.size());
        } else {
            // 非开场阶段保持原有逻辑（轮次取模）
            index = round % stageScripts.size();
        }
        return stageScripts.get(index);
    }

    // 检查是否识破
    private boolean isAntiFraud(String message) {
        String[] keywords = {"报警", "举报", "骗子", "诈骗", "110", "公安局",
                "不信", "骗人", "假的", "别骗我", "反诈", "核实"};
        return Arrays.stream(keywords).anyMatch(message::contains);
    }

    /*// 获取套路提示
    private String getScamTip(String scene, String stage) {
        Map<String, String> tips = new HashMap<>();
        tips.put("刷单诈骗", "套路：以小利引诱，逐步加大投入，最后卷款跑路");
        tips.put("冒充客服", "套路：谎称退款，诱导点击钓鱼链接，骗取验证码");
        tips.put("网贷诈骗", "套路：低门槛吸引，以各种名目收费，最后消失");
        tips.put("杀猪盘", "套路：建立感情-展示实力-诱导投资-卷款消失");
        tips.put("冒充公检法", "套路：制造恐慌-要求配合-资金转移");
        return tips.getOrDefault(scene, "警惕诈骗套路");
    }*/

    // AI润色
    private String enhanceWithAI(String script, String userMessage) {
        // 简单的润色规则
        if (script.contains("亲")) {
            return script.replace("亲", "亲爱的");
        }
        if (script.contains("抓紧时间")) {
            return script + " 机会难得哦~";
        }
        return script;
    }
}