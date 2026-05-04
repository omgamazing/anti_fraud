package com.example.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FraudScriptService {

    // 获取场景描述（供参考使用）
    public String getSceneDescription(String scene) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("刷单诈骗", "骗子会先用小额返利建立信任，然后诱导做大额联单，最后以卡单、冻结为由要求继续转账。");
        descriptions.put("冒充客服", "骗子谎称商品质量问题或快递丢失，主动赔偿，引导点击钓鱼链接或索要验证码。");
        descriptions.put("网贷诈骗", "骗子声称无抵押秒到账，然后以解冻费、保证金、手续费等名义反复收费。");
        descriptions.put("杀猪盘", "骗子先建立感情，然后展示投资赚钱能力，诱导参与虚假投资，小额盈利后骗大钱。");
        descriptions.put("冒充公检法", "骗子语气强硬制造恐慌，声称涉嫌犯罪，要求资金转入安全账户验证。");
        return descriptions.getOrDefault(scene, "诈骗场景");
    }
}