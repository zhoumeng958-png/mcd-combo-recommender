package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.config.RecommenderProperties;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 性价比策略
 * <p>
 * 预算区间已经是**硬门槛**（区间外的套餐在打分之前就被淘汰），
 * 所以「是否在预算内」不再需要打分——给每个候选都打 100 分等于白占一个权重，
 * 而且会把所有人的分数一起往 100 拉、压缩其他维度的分差。
 * <p>
 * 这一维改回答另一个问题：**同样的预算里，谁更划算？**
 * <p>
 * 指标用「每元热量」（kcal / 元）——花同样的钱能换回多少能量。
 * 得分映射到固定区间（{@code recommender.value-kcal-per-yuan-min} ~
 * {@code -max}，默认 20 ~ 30 kcal/元 对应 50 ~ 100 分），而不是在候选集内做归一化。
 * <p>
 * 之所以不用 min-max 归一化：候选集只有两三个时（比如预算收窄到 ¥50-60），
 * 两个套餐每元热量只差 1.6%，归一化却会把它们拉成 100 分和 50 分，
 * 等于凭空造出一个巨大的差距。用固定区间则「差多少分多少分」，更可预期。
 */
@Component
@RequiredArgsConstructor
public class ValueMatchStrategy implements RecommendStrategy {

    /** 每元热量低于下界时的得分 */
    private static final double FLOOR = 50D;

    /** 每元热量达到上界时的得分 */
    private static final double CEIL = 100D;

    /** 判定「并列最优」的浮点容差 */
    private static final double EPS = 1e-6;

    private final RecommenderProperties properties;

    @Override
    public String name() {
        return "value";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean score(RecommendContext context) {
        final double lo = properties.getValueKcalPerYuanMin();
        double hi = properties.getValueKcalPerYuanMax();
        if (hi <= lo) {
            // 配置写反了也不至于除零，给一个最小的有效跨度
            hi = lo + 1D;
        }

        // 1. 逐个打分：比值落在 [lo, hi] 之外会被夹到端点，因此天然不会超出 50~100
        Double best = null;
        int comparable = 0;
        for (Combo combo : context.getCandidates()) {
            Double value = kcalPerYuan(combo);
            if (value == null) {
                continue;
            }
            comparable++;
            if (best == null || value > best) {
                best = value;
            }
            double ratio = (value - lo) / (hi - lo);
            ratio = Math.max(0D, Math.min(1D, ratio));
            context.addScore(combo.getId(), name(), FLOOR + (CEIL - FLOOR) * ratio);
        }
        if (comparable == 0) {
            // 热量或价格全缺，本策略不参与（不计入权重分母）
            return false;
        }

        // 2. 加推荐理由。
        //    只给「相对最优」加的话有个问题：最优的那个可能因为口味/忌口等原因
        //    根本进不了 TopN，用户就永远看不到这条理由。
        //    所以再给「每元热量达到参考区间中点」的候选加一条稍弱的理由，
        //    保证性价比这个维度在推荐理由里是看得见的。
        final double mid = (lo + hi) / 2D;
        for (Combo combo : context.getCandidates()) {
            Double value = kcalPerYuan(combo);
            if (value == null) {
                continue;
            }
            if (comparable > 1 && best != null && Math.abs(value - best) < EPS) {
                context.addReason(combo.getId(), String.format(
                        "性价比最高：每元能买到 %.1f kcal，同价位里最划算", value));
            } else if (value >= mid) {
                context.addReason(combo.getId(), String.format(
                        "性价比不错：每元能买到 %.1f kcal", value));
            }
        }
        return true;
    }

    /**
     * 每元热量 = 套餐热量 / 套餐价。
     * 热量或价格缺失、价格非正时返回 {@code null}（该候选不参与本策略）。
     */
    private static Double kcalPerYuan(Combo combo) {
        if (combo.getCalories() == null || combo.getPrice() == null) {
            return null;
        }
        double price = combo.getPrice().doubleValue();
        if (price <= 0D) {
            return null;
        }
        return combo.getCalories() / price;
    }
}
