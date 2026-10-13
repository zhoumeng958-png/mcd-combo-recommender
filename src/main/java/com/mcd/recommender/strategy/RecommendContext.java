package com.mcd.recommender.strategy;

import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.enums.ProductCategory;
import com.mcd.recommender.enums.TimeSlot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 推荐上下文：在一条流水线上被各个策略读写
 * <p>
 * 设计要点：每个策略只负责给候选套餐打 0-100 的「单项分」，
 * 由 {@link StrategyEngine} 按权重做加权平均得到最终分，
 * 策略之间互不感知，新增策略只需实现接口并交给 Spring 管理。
 */
@Getter
public class RecommendContext {

    /** 原始请求 */
    private final RecommendRequest request;

    /** 候选套餐（已过滤下架/不符合硬性条件的） */
    private final List<Combo> candidates;

    /** 实际生效的时段 */
    private final TimeSlot effectiveTimeSlot;

    /** comboId -> (策略名 -> 单项分) */
    private final Map<Long, Map<String, Double>> strategyScores = new HashMap<>();

    /** comboId -> 推荐理由 */
    private final Map<Long, List<String>> reasons = new HashMap<>();

    /** 生效策略的权重：策略名 -> 权重 */
    private final Map<String, Double> effectiveWeights = new LinkedHashMap<>();

    /**
     * comboId -> 该套餐包含的单品分类集合。
     * <p>
     * 由 Service 在打分前批量装载（复用套餐列表装配时的那次查询，不额外增加 SQL），
     * 供策略判断「套餐内容」用——例如儿童友好策略需要知道是否含甜品/冰淇淋。
     */
    private final Map<Long, Set<ProductCategory>> comboCategories = new HashMap<>();

    public void putCategories(Long comboId, Set<ProductCategory> categories) {
        comboCategories.put(comboId, categories);
    }

    /**
     * 取某套餐包含的单品分类，未装载时返回空集合（策略需自行容错）
     */
    public Set<ProductCategory> categoriesOf(Long comboId) {
        return comboCategories.getOrDefault(comboId, Collections.emptySet());
    }

    public RecommendContext(RecommendRequest request, List<Combo> candidates, TimeSlot effectiveTimeSlot) {
        this.request = request;
        this.candidates = candidates;
        this.effectiveTimeSlot = effectiveTimeSlot;
    }

    /**
     * 记录某套餐在某策略下的单项得分
     */
    public void addScore(Long comboId, String strategyName, double score) {
        strategyScores.computeIfAbsent(comboId, k -> new LinkedHashMap<>())
                .put(strategyName, round(clamp(score)));
    }

    /**
     * 追加一条推荐理由
     */
    public void addReason(Long comboId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return;
        }
        reasons.computeIfAbsent(comboId, k -> new ArrayList<>()).add(reason);
    }

    public List<String> getReasons(Long comboId) {
        return reasons.getOrDefault(comboId, Collections.emptyList());
    }

    public Map<String, Double> getStrategyScores(Long comboId) {
        return strategyScores.getOrDefault(comboId, Collections.emptyMap());
    }

    public void setWeight(String strategyName, double weight) {
        effectiveWeights.put(strategyName, weight);
    }

    private static double clamp(double v) {
        return Math.max(0D, Math.min(100D, v));
    }

    private static double round(double v) {
        return Math.round(v * 100D) / 100D;
    }
}
