package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 预算匹配策略
 * <p>
 * 套餐价格落在用户预算区间内得满分；略超预算（10% 以内）给过渡分；
 * 大幅超预算快速衰减，避免推荐出「看着好但买不起」的套餐。
 */
@Slf4j
@Component
public class BudgetMatchStrategy implements RecommendStrategy {

    /** 允许超出预算的比例上限 */
    private static final double TOLERANCE = 0.10D;

    @Override
    public String name() {
        return "budget";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean score(RecommendContext context) {
        RecommendRequest request = context.getRequest();
        BigDecimal max = request.getBudgetMax();
        if (max == null || max.compareTo(BigDecimal.ZERO) <= 0) {
            // 用户没给预算上限，本策略不参与
            return false;
        }
        BigDecimal min = request.getBudgetMin();

        for (Combo combo : context.getCandidates()) {
            BigDecimal price = combo.getPrice();
            if (price == null) {
                continue;
            }
            double score;
            if (price.compareTo(max) > 0) {
                // 超出预算
                double over = price.subtract(max).doubleValue() / max.doubleValue();
                if (over <= TOLERANCE) {
                    score = 55D;
                    context.addReason(combo.getId(), String.format(
                            "价格 ¥%s 略高于预算上限 ¥%s，但配置更丰富", fmt(price), fmt(max)));
                } else {
                    score = Math.max(0D, 35D - over * 100D);
                    context.addReason(combo.getId(), String.format(
                            "价格 ¥%s 超出预算 ¥%s 较多", fmt(price), fmt(max)));
                }
            } else if (min != null && price.compareTo(min) < 0) {
                // 低于预算下限：省钱，但可能配置偏简单
                score = 85D;
            } else {
                score = 100D;
                context.addReason(combo.getId(), String.format(
                        "价格 ¥%s 正好落在你的预算区间内", fmt(price)));
            }
            context.addScore(combo.getId(), name(), score);
        }
        return true;
    }

    private static String fmt(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
