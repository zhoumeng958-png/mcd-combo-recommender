package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import org.springframework.stereotype.Component;

/**
 * 人数适配策略
 * <p>
 * 套餐建议人数与用户实际就餐需求越接近分越高，避免一人点全家桶、四人点单人餐。
 * <p>
 * 儿童按 {@link #CHILD_FACTOR} 折算成等效人数：儿童饭量约为大人的六成，
 * 只按人头数算会把分量估高，容易点多吃不完。例如「2 大人 + 1 儿童」
 * 按 2.6 人份匹配，3 人份套餐即为最优，而不是去找 3 人份以上。
 */
@Component
public class PeopleFitStrategy implements RecommendStrategy {

    /** 儿童饭量折算系数：1 个儿童 ≈ 0.6 个大人 */
    public static final double CHILD_FACTOR = 0.6D;

    @Override
    public String name() {
        return "people";
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public boolean score(RecommendContext context) {
        RecommendRequest request = context.getRequest();
        Integer adults = request.getPeopleCount();
        if (adults == null || adults <= 0) {
            return false;
        }
        int children = request.getChildCount() == null ? 0 : Math.max(0, request.getChildCount());

        // 等效就餐人数 = 大人 + 儿童 × 0.6
        double effective = adults + children * CHILD_FACTOR;

        String who = children > 0
                ? String.format("%d 大人 + %d 儿童（约 %.1f 人份）", adults, children, effective)
                : String.format("%d 人", adults);

        for (Combo combo : context.getCandidates()) {
            int comboPeople = combo.getPeopleCount() == null ? 1 : combo.getPeopleCount();
            double diff = Math.abs(comboPeople - effective);

            double score;
            if (diff < 0.5D) {
                score = 100D;
                context.addReason(combo.getId(), String.format("分量刚好适合 %s", who));
            } else if (diff < 1.5D) {
                score = 70D;
            } else if (diff < 2.5D) {
                score = 40D;
            } else {
                score = 15D;
                context.addReason(combo.getId(), String.format(
                        "建议 %d 人食用，与你需要的 %s 分量差距较大，可搭配其他套餐一起点",
                        comboPeople, who));
            }
            context.addScore(combo.getId(), name(), score);
        }
        return true;
    }
}
