package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import com.mcd.recommender.util.TagUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 口味匹配策略
 * <p>
 * 命中偏好标签越多分越高；命中忌口标签则大幅扣分，保证「不吃辣」的用户不会拿到辣味套餐。
 */
@Component
public class TasteMatchStrategy implements RecommendStrategy {

    /** 口味基准分：一个偏好标签都没命中时的分数 */
    private static final double BASE = 50D;

    /** 每个忌口标签的扣分 */
    private static final double AVOID_PENALTY = 45D;

    @Override
    public String name() {
        return "taste";
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public boolean score(RecommendContext context) {
        RecommendRequest request = context.getRequest();
        List<String> likes = request.getTasteTags();
        List<String> avoids = request.getAvoidTags();
        boolean hasLike = likes != null && !likes.isEmpty();
        boolean hasAvoid = avoids != null && !avoids.isEmpty();
        if (!hasLike && !hasAvoid) {
            return false;
        }

        for (Combo combo : context.getCandidates()) {
            List<String> comboTags = TagUtils.split(combo.getTasteTags());
            double score = BASE;

            if (hasLike) {
                List<String> hits = TagUtils.intersect(likes, comboTags);
                double hitRate = (double) hits.size() / likes.size();
                score = BASE + (100D - BASE) * hitRate;
                if (!hits.isEmpty()) {
                    context.addReason(combo.getId(), "口味契合：" + String.join("、", hits));
                }
            }

            if (hasAvoid) {
                List<String> bad = TagUtils.intersect(avoids, comboTags);
                if (!bad.isEmpty()) {
                    score -= AVOID_PENALTY * bad.size();
                    context.addReason(combo.getId(), "含忌口标签：" + String.join("、", bad));
                }
            }

            context.addScore(combo.getId(), name(), score);
        }
        return true;
    }
}
