package com.mcd.recommender;

import com.mcd.recommender.config.RecommenderProperties;
import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.enums.TimeSlot;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import com.mcd.recommender.strategy.ScoredCombo;
import com.mcd.recommender.strategy.StrategyEngine;
import com.mcd.recommender.strategy.impl.BudgetMatchStrategy;
import com.mcd.recommender.strategy.impl.DiversityStrategy;
import com.mcd.recommender.strategy.impl.PeopleFitStrategy;
import com.mcd.recommender.strategy.impl.PopularityStrategy;
import com.mcd.recommender.strategy.impl.TasteMatchStrategy;
import com.mcd.recommender.strategy.impl.TimeSlotMatchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 推荐引擎单元测试（不依赖 Spring 容器与数据库）
 */
class StrategyEngineTest {

    private StrategyEngine engine;

    @BeforeEach
    void setUp() {
        RecommenderProperties properties = new RecommenderProperties();
        properties.setTopN(5);
        List<RecommendStrategy> strategies = Arrays.asList(
                new BudgetMatchStrategy(),
                new TasteMatchStrategy(),
                new PeopleFitStrategy(),
                new TimeSlotMatchStrategy(),
                new PopularityStrategy(),
                new DiversityStrategy());
        engine = new StrategyEngine(strategies, properties);
    }

    @Test
    @DisplayName("预算内 + 口味命中 + 单人份 应排在首位")
    void testRecommendRanking() {
        Combo classic = combo(1L, "巨无霸中套餐", "43.50", 1, "经典,牛肉", 12000, "4.8");
        Combo spicy = combo(2L, "麦辣鸡腿堡中套餐", "41.50", 1, "香辣,鸡肉", 15000, "4.9");
        Combo family = combo(3L, "家庭欢聚餐", "158.00", 4, "经典,香辣", 3000, "4.8");

        RecommendRequest request = new RecommendRequest();
        request.setBudgetMax(new BigDecimal("50"));
        request.setPeopleCount(1);
        request.setTasteTags(Arrays.asList("经典", "牛肉"));

        RecommendContext context = new RecommendContext(
                request, Arrays.asList(classic, spicy, family), TimeSlot.LUNCH);
        List<ScoredCombo> result = engine.recommend(context);

        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getCombo().getId(), "巨无霸中套餐应排第一");
        assertTrue(result.get(0).getScore() > result.get(result.size() - 1).getScore(), "得分应递减");
    }

    @Test
    @DisplayName("忌口标签应显著压低对应套餐得分")
    void testAvoidTagPenalty() {
        Combo spicy = combo(2L, "麦辣鸡腿堡中套餐", "41.50", 1, "香辣,鸡肉", 15000, "4.9");
        Combo classic = combo(1L, "巨无霸中套餐", "43.50", 1, "经典,牛肉", 12000, "4.8");

        RecommendRequest request = new RecommendRequest();
        request.setAvoidTags(Arrays.asList("香辣"));
        request.setPeopleCount(1);

        RecommendContext context = new RecommendContext(
                request, Arrays.asList(spicy, classic), TimeSlot.LUNCH);
        List<ScoredCombo> result = engine.recommend(context);

        assertEquals(1L, result.get(0).getCombo().getId(), "忌口香辣后应推荐不辣的套餐");
    }

    @Test
    @DisplayName("超预算套餐得分应低于预算内套餐")
    void testBudgetFilter() {
        Combo cheap = combo(1L, "麦香鱼中套餐", "38.00", 1, "清淡,鱼", 6000, "4.5");
        Combo expensive = combo(7L, "安格斯厚牛堡中套餐", "54.00", 1, "经典,牛肉", 4000, "4.8");

        RecommendRequest request = new RecommendRequest();
        request.setBudgetMax(new BigDecimal("40"));
        request.setPeopleCount(1);

        RecommendContext context = new RecommendContext(
                request, Arrays.asList(cheap, expensive), TimeSlot.LUNCH);
        List<ScoredCombo> result = engine.recommend(context);

        assertEquals(1L, result.get(0).getCombo().getId(), "预算内套餐应排第一");
    }

    private Combo combo(Long id, String name, String price, int peopleCount,
                        String tasteTags, int salesCount, String rating) {
        Combo combo = new Combo();
        combo.setId(id);
        combo.setName(name);
        combo.setPrice(new BigDecimal(price));
        combo.setOriginalPrice(new BigDecimal(price).add(new BigDecimal("5")));
        combo.setPeopleCount(peopleCount);
        combo.setTasteTags(tasteTags);
        combo.setTimeSlots("ALL_DAY");
        combo.setSalesCount(salesCount);
        combo.setRating(new BigDecimal(rating));
        combo.setStatus(1);
        return combo;
    }
}
