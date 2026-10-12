package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import org.springframework.stereotype.Component;

/**
 * 人数适配策略
 * <p>
 * 套餐建议人数与用户就餐人数越接近分越高，避免一人点全家桶、四人点单人餐。
 */
@Component
public class PeopleFitStrategy implements RecommendStrategy {

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
        Integer people = request.getPeopleCount();
        if (people == null || people <= 0) {
            return false;
        }

        for (Combo combo : context.getCandidates()) {
            int comboPeople = combo.getPeopleCount() == null ? 1 : combo.getPeopleCount();
            int diff = Math.abs(comboPeople - people);

            double score;
            if (diff == 0) {
                score = 100D;
                context.addReason(combo.getId(), String.format("分量刚好适合 %d 人享用", people));
            } else if (diff == 1) {
                score = 70D;
            } else if (diff == 2) {
                score = 40D;
            } else {
                score = 15D;
                context.addReason(combo.getId(), String.format(
                        "建议 %d 人食用，与你选的 %d 人份量差距较大", comboPeople, people));
            }
            context.addScore(combo.getId(), name(), score);
        }
        return true;
    }
}
