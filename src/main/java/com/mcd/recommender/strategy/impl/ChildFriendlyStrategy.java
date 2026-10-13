package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.enums.ProductCategory;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 儿童友好策略
 * <p>
 * 只在请求里带了儿童时生效；没带儿童直接返回 false，这样它既不打分、
 * 权重也不计入分母（避免中性分稀释其他策略）。
 * <p>
 * 打分考虑三件事：
 * <ol>
 *   <li>口味是否偏甜 / 清淡 / 奶香——儿童接受度高；</li>
 *   <li>是否辛辣重口——儿童一般不吃；</li>
 *   <li>套餐名是否明确是儿童餐——强信号，直接加分并给出理由。</li>
 * </ol>
 */
@Component
public class ChildFriendlyStrategy implements RecommendStrategy {

    /** 儿童普遍接受的口味 */
    private static final Set<String> KID_LIKE =
            new HashSet<>(Arrays.asList("甜", "清淡", "醇香", "芝士"));

    /** 儿童普遍抗拒的口味 */
    private static final Set<String> KID_DISLIKE =
            new HashSet<>(Arrays.asList("香辣", "咸香", "厚实"));

    /** 基础分：没有明显倾向时给中性偏上的分 */
    private static final double BASE = 62D;

    @Override
    public String name() {
        return "childFriendly";
    }

    @Override
    public int order() {
        return 70;
    }

    @Override
    public boolean score(RecommendContext context) {
        RecommendRequest request = context.getRequest();
        int children = request.getChildCount() == null ? 0 : request.getChildCount();
        if (children <= 0) {
            return false;
        }

        for (Combo combo : context.getCandidates()) {
            double score = BASE;
            boolean liked = false;

            String tasteTags = combo.getTasteTags() == null ? "" : combo.getTasteTags();
            for (String raw : tasteTags.split(",")) {
                String tag = raw.trim();
                if (KID_LIKE.contains(tag)) {
                    score += 12D;
                    liked = true;
                } else if (KID_DISLIKE.contains(tag)) {
                    score -= 22D;
                }
            }

            // 辣度：儿童通常接受不了
            Integer spicy = combo.getSpicyLevel();
            if (spicy != null && spicy >= 2) {
                score -= 25D;
            } else if (spicy != null && spicy == 1) {
                score -= 8D;
            }

            // 含甜品 / 冰淇淋：小朋友普遍更买账。
            // 注意：光看 tasteTags 是看不出来的——「巨无霸四件套」的标签是「经典,牛肉」，
            // 但它比「巨无霸中套餐」多一个原味甜筒，这才是带娃场景下的关键差异。
            if (context.categoriesOf(combo.getId()).contains(ProductCategory.DESSERT)) {
                score += 15D;
                context.addReason(combo.getId(), "套餐里含甜品 / 冰淇淋，小朋友更买账");
            }

            String comboName = combo.getName() == null ? "" : combo.getName();
            if (comboName.contains("儿童") || comboName.contains("开心乐园")) {
                score += 20D;
                context.addReason(combo.getId(), "儿童套餐，分量和口味都是按小朋友设计的");
            } else if (liked) {
                context.addReason(combo.getId(), "口味偏甜 / 清淡，小朋友比较容易接受");
            }

            context.addScore(combo.getId(), name(), score);
        }
        return true;
    }
}
