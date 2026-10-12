package com.mcd.recommender.strategy;

import com.mcd.recommender.config.RecommenderProperties;
import com.mcd.recommender.entity.Combo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推荐策略引擎
 * <p>
 * 职责：
 * 1. 按 order 依次执行所有策略，收集「单项分」；
 * 2. 按配置权重对单项分做加权平均，得到 0-100 的综合得分；
 * 3. 按综合得分倒序输出。
 * <p>
 * 单个策略抛异常不会影响整体推荐，只会被跳过并记录日志。
 */
@Slf4j
@Component
public class StrategyEngine {

    private final List<RecommendStrategy> strategies;
    private final RecommenderProperties properties;

    public StrategyEngine(List<RecommendStrategy> strategies, RecommenderProperties properties) {
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(RecommendStrategy::order))
                .collect(Collectors.toList());
        this.properties = properties;
        log.info("推荐引擎已加载 {} 个策略: {}", this.strategies.size(),
                this.strategies.stream().map(RecommendStrategy::name).collect(Collectors.toList()));
    }

    /**
     * 执行推荐打分
     *
     * @param context 推荐上下文
     * @return 按综合得分倒序的套餐列表
     */
    public List<ScoredCombo> recommend(RecommendContext context) {
        // 1. 依次执行各策略
        for (RecommendStrategy strategy : strategies) {
            try {
                boolean effective = strategy.score(context);
                if (effective) {
                    context.setWeight(strategy.name(), properties.weightOf(strategy.name()));
                }
            } catch (Exception e) {
                log.error("策略 [{}] 执行异常，已跳过", strategy.name(), e);
            }
        }

        // 2. 加权平均得到综合得分
        List<ScoredCombo> result = new ArrayList<>(context.getCandidates().size());
        for (Combo combo : context.getCandidates()) {
            ScoredCombo scored = new ScoredCombo(combo);
            Map<String, Double> detail = context.getStrategyScores(combo.getId());

            double weightedSum = 0D;
            double weightTotal = 0D;
            Map<String, Double> orderedDetail = new LinkedHashMap<>();
            for (Map.Entry<String, Double> entry : detail.entrySet()) {
                Double weight = context.getEffectiveWeights().get(entry.getKey());
                if (weight == null) {
                    continue;
                }
                weightedSum += entry.getValue() * weight;
                weightTotal += weight;
                orderedDetail.put(entry.getKey(), entry.getValue());
            }

            double finalScore = weightTotal <= 0D ? 60D : weightedSum / weightTotal;
            scored.setScore(round(clamp(finalScore)));
            scored.setScoreDetail(orderedDetail);
            scored.setReasons(context.getReasons(combo.getId()));
            result.add(scored);
        }

        // 3. 得分倒序（同分按销量高的在前）
        result.sort(Comparator
                .comparingDouble(ScoredCombo::getScore).reversed()
                .thenComparing(sc -> sc.getCombo().getSalesCount() == null ? 0 : sc.getCombo().getSalesCount(),
                        Comparator.reverseOrder()));
        return result;
    }

    private static double clamp(double v) {
        return Math.max(0D, Math.min(100D, v));
    }

    private static double round(double v) {
        return Math.round(v * 100D) / 100D;
    }
}
