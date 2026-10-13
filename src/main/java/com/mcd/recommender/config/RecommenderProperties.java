package com.mcd.recommender.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 推荐引擎可调参数，对应 application.yml 中的 recommender.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "recommender")
public class RecommenderProperties {

    /** 返回给前端的推荐结果数量 */
    private int topN = 5;

    /**
     * 热量档位分界线（kcal）：≤ 该值算「热量偏低」，&gt; 该值算「热量偏高」。
     * 对应用户在页面上选择的「热量偏低 / 热量偏高」。
     */
    private int calorieThreshold = 1000;

    /**
     * 性价比策略的「每元热量」下界（kcal/元）：≤ 该值记 50 分。
     * 与 {@link #valueKcalPerYuanMax} 一起构成线性映射区间。
     */
    private double valueKcalPerYuanMin = 20D;

    /**
     * 性价比策略的「每元热量」上界（kcal/元）：≥ 该值记 100 分。
     */
    private double valueKcalPerYuanMax = 30D;

    /**
     * 各策略权重：key 为策略名（value/taste/people/timeSlot/popularity/diversity/childFriendly）
     */
    private Map<String, Double> weights = new LinkedHashMap<>();

    /**
     * 取某策略的权重，未配置时返回默认值 1.0。
     * <p>
     * 注意：配置里可能写成 kebab-case（如 {@code time-slot}），
     * 而策略名是 camelCase（{@code timeSlot}）。Map 的键**不会**经过 Spring 的宽松绑定，
     * 直接 {@code containsKey} 会漏配，导致权重被静默忽略、回退成 1.0。
     * 因此这里做一次归一化匹配（去掉 - 和 _ 并转小写）。
     */
    public double weightOf(String strategyName) {
        if (weights == null || weights.isEmpty() || strategyName == null) {
            return 1.0D;
        }
        Double w = weights.get(strategyName);
        if (w == null) {
            String target = normalize(strategyName);
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                if (normalize(entry.getKey()).equals(target)) {
                    w = entry.getValue();
                    break;
                }
            }
        }
        return w == null ? 1.0D : w;
    }

    private static String normalize(String key) {
        return key == null ? "" : key.replace("-", "").replace("_", "").toLowerCase();
    }
}
