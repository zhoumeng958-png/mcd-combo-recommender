package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 人气热度策略
 * <p>
 * 销量做对数归一化（避免头部套餐碾压式压制），再与评分加权，
 * 让「大家点得多 + 评价好」的套餐获得自然加成。
 */
@Component
public class PopularityStrategy implements RecommendStrategy {

    /** 销量权重 */
    private static final double SALES_WEIGHT = 0.6D;

    /** 评分权重 */
    private static final double RATING_WEIGHT = 0.4D;

    /** 加推荐理由的分数门槛 */
    private static final double REASON_THRESHOLD = 85D;

    @Override
    public String name() {
        return "popularity";
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public boolean score(RecommendContext context) {
        int maxSales = 1;
        for (Combo combo : context.getCandidates()) {
            maxSales = Math.max(maxSales, nvl(combo.getSalesCount()));
        }
        double logMax = Math.log10(maxSales + 1D);

        for (Combo combo : context.getCandidates()) {
            int sales = nvl(combo.getSalesCount());
            double salesScore = logMax <= 0D ? 50D : Math.log10(sales + 1D) / logMax * 100D;
            double rating = combo.getRating() == null ? 5D : combo.getRating().doubleValue();
            double ratingScore = rating / 5D * 100D;

            double score = salesScore * SALES_WEIGHT + ratingScore * RATING_WEIGHT;
            context.addScore(combo.getId(), name(), score);

            if (score >= REASON_THRESHOLD) {
                context.addReason(combo.getId(), String.format(
                        "人气爆款：累计售出 %d 份，评分 %.1f", sales, rating));
            }
        }
        return true;
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static double nvl(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }
}
