package com.mcd.recommender;

import com.mcd.recommender.config.RecommenderProperties;
import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.enums.TimeSlot;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import com.mcd.recommender.strategy.ScoredCombo;
import com.mcd.recommender.strategy.StrategyEngine;
import com.mcd.recommender.strategy.impl.DiversityStrategy;
import com.mcd.recommender.strategy.impl.PeopleFitStrategy;
import com.mcd.recommender.strategy.impl.PopularityStrategy;
import com.mcd.recommender.strategy.impl.TasteMatchStrategy;
import com.mcd.recommender.strategy.impl.TimeSlotMatchStrategy;
import com.mcd.recommender.strategy.impl.ValueMatchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 推荐引擎单元测试（不依赖 Spring 容器与数据库）
 * <p>
 * 注意：预算区间已改为「硬过滤」，由 RecommendServiceImpl 在打分之前淘汰，
 * 不再是一个打分维度；「价格」维度的打分由 ValueMatchStrategy（性价比）承担。
 */
class StrategyEngineTest {

    private StrategyEngine engine;

    @BeforeEach
    void setUp() {
        RecommenderProperties properties = new RecommenderProperties();
        properties.setTopN(5);
        properties.setValueKcalPerYuanMin(20D);
        properties.setValueKcalPerYuanMax(30D);
        List<RecommendStrategy> strategies = Arrays.asList(
                new ValueMatchStrategy(properties),
                new TasteMatchStrategy(),
                new PeopleFitStrategy(),
                new TimeSlotMatchStrategy(),
                new PopularityStrategy(),
                new DiversityStrategy());
        engine = new StrategyEngine(strategies, properties);
    }

    @Test
    @DisplayName("口味命中 + 单人份 应排在首位")
    void testRecommendRanking() {
        Combo classic = combo(1L, "巨无霸中套餐", "43.50", 1, "经典,牛肉", 12000, "4.8", 1100);
        Combo spicy = combo(2L, "麦辣鸡腿堡中套餐", "41.50", 1, "香辣,鸡肉", 15000, "4.9", 1120);
        Combo family = combo(3L, "家庭欢聚餐", "158.00", 4, "经典,香辣", 3000, "4.8", 4000);

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
        // 两个套餐的「每元热量」刻意设成一样，避免性价比维度干扰本用例
        Combo spicy = combo(2L, "麦辣鸡腿堡中套餐", "41.50", 1, "香辣,鸡肉", 15000, "4.9", 1038);
        Combo classic = combo(1L, "巨无霸中套餐", "43.50", 1, "经典,牛肉", 12000, "4.8", 1088);

        RecommendRequest request = new RecommendRequest();
        request.setAvoidTags(Arrays.asList("香辣"));
        request.setPeopleCount(1);

        RecommendContext context = new RecommendContext(
                request, Arrays.asList(spicy, classic), TimeSlot.LUNCH);
        List<ScoredCombo> result = engine.recommend(context);

        assertEquals(1L, result.get(0).getCombo().getId(), "忌口香辣后应推荐不辣的套餐");
    }

    @Test
    @DisplayName("性价比：同价位下每元热量越高得分越高")
    void testValuePerYuan() {
        // 除热量外全部相同，唯一变量是「每元热量」
        // 高：40 元 1200 kcal = 30 kcal/元（到达上界 -> 100 分）
        // 低：40 元  600 kcal = 15 kcal/元（低于下界 -> 50 分）
        Combo high = combo(1L, "高性价比套餐", "40.00", 1, "经典", 10000, "4.7", 1200);
        Combo low = combo(2L, "低性价比套餐", "40.00", 1, "经典", 10000, "4.7", 600);

        RecommendRequest request = new RecommendRequest();
        request.setPeopleCount(1);

        RecommendContext context = new RecommendContext(
                request, Arrays.asList(low, high), TimeSlot.LUNCH);
        List<ScoredCombo> result = engine.recommend(context);

        assertEquals(1L, result.get(0).getCombo().getId(), "每元热量高的应排第一");
        assertTrue(result.get(0).getScore() > result.get(1).getScore(), "得分应有差距");

        Map<String, Double> detail = result.get(0).getScoreDetail();
        assertTrue(detail.containsKey("value"), "得分明细里应有 value 维度");
        assertEquals(100D, detail.get("value"), 0.01D, "到达上界应记 100 分");
        assertEquals(50D, result.get(1).getScoreDetail().get("value"), 0.01D,
                "低于下界应记 50 分");
    }

    @Test
    @DisplayName("性价比：热量缺失时不参与，不产生 value 维度")
    void testValueSkippedWhenCaloriesMissing() {
        Combo noCalories = combo(1L, "无热量套餐", "40.00", 1, "经典", 10000, "4.7", null);

        RecommendRequest request = new RecommendRequest();
        request.setPeopleCount(1);

        RecommendContext context = new RecommendContext(
                request, Arrays.asList(noCalories), TimeSlot.LUNCH);
        List<ScoredCombo> result = engine.recommend(context);

        assertFalse(result.isEmpty());
        assertFalse(result.get(0).getScoreDetail().containsKey("value"),
                "热量缺失时不应出现 value 维度");
    }

    private Combo combo(Long id, String name, String price, int peopleCount,
                        String tasteTags, int salesCount, String rating, Integer calories) {
        Combo combo = new Combo();
        combo.setId(id);
        combo.setName(name);
        combo.setPrice(new BigDecimal(price));
        combo.setOriginalPrice(new BigDecimal(price).add(new BigDecimal("5")));
        combo.setPeopleCount(peopleCount);
        combo.setCalories(calories);
        combo.setTasteTags(tasteTags);
        combo.setTimeSlots("ALL_DAY");
        combo.setSalesCount(salesCount);
        combo.setRating(new BigDecimal(rating));
        combo.setStatus(1);
        return combo;
    }
}
