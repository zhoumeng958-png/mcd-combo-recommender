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
     * 各策略权重：key 为策略名（budget/taste/people/timeSlot/popularity/diversity）
     */
    private Map<String, Double> weights = new LinkedHashMap<>();

    public double weightOf(String strategyName) {
        if (weights == null || !weights.containsKey(strategyName)) {
            return 1.0D;
        }
        Double w = weights.get(strategyName);
        return w == null ? 1.0D : w;
    }
}
