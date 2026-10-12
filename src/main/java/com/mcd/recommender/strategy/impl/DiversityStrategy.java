package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import com.mcd.recommender.util.TagUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 多样性策略
 * <p>
 * 统计候选集中各主料（牛肉/鸡肉/鱼/猪肉）的分布，
 * 主料越「稀缺」的套餐得分越高，避免最终榜单被同一种主料霸榜，
 * 让用户看到更丰富的选择。
 */
@Component
public class DiversityStrategy implements RecommendStrategy {

    /** 稀有度基准分 */
    private static final double BASE = 20D;

    @Override
    public String name() {
        return "diversity";
    }

    @Override
    public int order() {
        return 60;
    }

    @Override
    public boolean score(RecommendContext context) {
        if (context.getCandidates().isEmpty()) {
            return false;
        }

        Map<String, Integer> ingredientCount = new HashMap<>();
        for (Combo combo : context.getCandidates()) {
            String main = TagUtils.mainIngredient(combo.getTasteTags());
            ingredientCount.merge(main, 1, Integer::sum);
        }

        int total = context.getCandidates().size();
        for (Combo combo : context.getCandidates()) {
            String main = TagUtils.mainIngredient(combo.getTasteTags());
            int count = ingredientCount.getOrDefault(main, 1);
            double ratio = (double) count / total;

            // 占比越低（越稀缺）得分越高
            double score = BASE + (100D - BASE) * (1D - ratio);
            context.addScore(combo.getId(), name(), score);

            if (ratio <= 0.34D && !TagUtils.OTHER_INGREDIENT.equals(main)) {
                context.addReason(combo.getId(), String.format("换个口味：以「%s」为主的套餐选择不多", main));
            }
        }
        return true;
    }
}
