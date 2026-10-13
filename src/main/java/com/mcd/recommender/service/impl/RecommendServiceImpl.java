package com.mcd.recommender.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.ResultCode;
import com.mcd.recommender.config.RecommenderProperties;
import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.entity.RecommendRecord;
import com.mcd.recommender.enums.CalorieLevel;
import com.mcd.recommender.enums.ProductCategory;
import com.mcd.recommender.enums.TimeSlot;
import com.mcd.recommender.mapper.ComboMapper;
import com.mcd.recommender.mapper.RecommendRecordMapper;
import com.mcd.recommender.service.ComboService;
import com.mcd.recommender.service.RecommendService;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.ScoredCombo;
import com.mcd.recommender.strategy.StrategyEngine;
import com.mcd.recommender.strategy.impl.PeopleFitStrategy;
import com.mcd.recommender.vo.ComboVO;
import com.mcd.recommender.vo.RecommendItemVO;
import com.mcd.recommender.vo.RecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 套餐推荐服务实现
 * <p>
 * 主流程：解析时段 -> 取候选 -> 硬性过滤 -> 策略引擎打分 -> 取 TopN -> 组装 VO -> 记录留痕
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final ComboMapper comboMapper;
    private final RecommendRecordMapper recommendRecordMapper;
    private final ComboService comboService;
    private final StrategyEngine strategyEngine;
    private final RecommenderProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public RecommendResponse recommend(RecommendRequest request) {
        long start = System.currentTimeMillis();

        // 1. 解析就餐时段：显式指定优先，否则按当前时间推断
        TimeSlot timeSlot = resolveTimeSlot(request.getTimeSlot());

        // 2. 取候选套餐（仅上架）
        List<Combo> candidates = comboMapper.selectList(
                new LambdaQueryWrapper<Combo>().eq(Combo::getStatus, 1));
        if (candidates.isEmpty()) {
            throw new BusinessException(ResultCode.RECOMMEND_NO_RESULT, "套餐库为空，请先初始化数据");
        }

        // 3. 硬性条件过滤（不满足直接淘汰，不参与打分）
        //    预算区间与热量档位都是「硬边界」，区间外的套餐不会出现在结果里。
        candidates = applyHardFilter(request, candidates);

        // 4./5. 装配 VO + 策略引擎打分
        //    过滤后可能一个都不剩（例如「预算 ¥15-35 + 热量偏高」这种组合），
        //    这属于正常的条件组合结果、不是错误，因此不抛异常，
        //    而是返回空结果 + 一句可执行的建议（buildSummary 已处理空列表）。
        Map<Long, ComboVO> comboVoMap = new LinkedHashMap<>();
        List<ScoredCombo> scoredList = new ArrayList<>();
        if (!candidates.isEmpty()) {
            // 批量装配 VO（一次查明细，避免 N+1）
            // 提前到打分之前：策略需要知道套餐「内容」（例如是否含甜品），
            // 而且取完 TopN 后还能复用这份结果，不额外增加查询。
            comboVoMap = comboService.toVOList(candidates).stream()
                    .collect(Collectors.toMap(ComboVO::getId, Function.identity(), (a, b) -> a));

            RecommendContext context = new RecommendContext(request, candidates, timeSlot);
            for (Combo combo : candidates) {
                ComboVO vo = comboVoMap.get(combo.getId());
                if (vo == null || vo.getItems() == null) {
                    continue;
                }
                Set<ProductCategory> categories = vo.getItems().stream()
                        .map(item -> ProductCategory.of(item.getCategory()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                context.putCategories(combo.getId(), categories);
            }
            scoredList = strategyEngine.recommend(context);
        }

        // 6. 截取 TopN
        //    注意：topN 只是「最多几条」，不是「必须凑够几条」。
        //    符合条件的有 3 个就返回 3 个，不会为了凑数把区间外的套餐补进来。
        int topN = request.getTopN() != null ? request.getTopN() : properties.getTopN();
        topN = Math.min(Math.max(topN, 0), scoredList.size());
        List<ScoredCombo> topList = scoredList.subList(0, topN);

        List<RecommendItemVO> items = new ArrayList<>(topList.size());
        int rank = 1;
        for (ScoredCombo scored : topList) {
            RecommendItemVO item = new RecommendItemVO();
            item.setRank(rank++);
            item.setCombo(comboVoMap.get(scored.getCombo().getId()));
            item.setScore(BigDecimal.valueOf(scored.getScore()).setScale(2, RoundingMode.HALF_UP));
            item.setReasons(scored.getReasons());

            Map<String, BigDecimal> detail = new LinkedHashMap<>();
            scored.getScoreDetail().forEach((k, v) ->
                    detail.put(k, BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP)));
            item.setScoreDetail(detail);
            items.add(item);
        }

        long costMs = System.currentTimeMillis() - start;

        // 7. 组装响应
        RecommendResponse response = new RecommendResponse();
        response.setUserId(request.getUserId());
        response.setTimeSlot(timeSlot.name());
        response.setTotalCandidates(candidates.size());
        response.setCostMs(costMs);
        response.setItems(items);
        response.setSummary(buildSummary(request, timeSlot, items));

        // 8. 记录留痕（失败不影响主流程）
        saveRecord(request, response);

        log.info("推荐完成: userId={}, 时段={}, 候选={}, 返回={}, 耗时={}ms",
                request.getUserId(), timeSlot.name(), candidates.size(), items.size(), costMs);
        return response;
    }

    @Override
    public List<RecommendRecord> recentRecords(String userId, Integer limit) {
        int size = (limit == null || limit < 1) ? 10 : Math.min(limit, 100);
        LambdaQueryWrapper<RecommendRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userId)) {
            wrapper.eq(RecommendRecord::getUserId, userId);
        }
        wrapper.orderByDesc(RecommendRecord::getCreateTime).last("limit " + size);
        return recommendRecordMapper.selectList(wrapper);
    }

    /* ------------------------------------------------------------------ */

    private TimeSlot resolveTimeSlot(String code) {
        TimeSlot slot = TimeSlot.of(code);
        return slot == null ? TimeSlot.current() : slot;
    }

    /**
     * 硬性过滤：预算区间 + 热量档位 + 热量上限，不满足的套餐直接淘汰、不参与打分。
     * <p>
     * 为什么预算要做硬过滤而不是只打分：用户选了 ¥15-35，就该只看到 ¥15-35 的套餐。
     * 若只靠打分扣分，区间外的套餐仍可能因其他维度得分高而挤进榜单，
     * 看起来就是「系统没按我选的价格来」。
     * <p>
     * 预算成为硬门槛之后，「价格」这一维的打分交给 value（性价比）策略——
     * 在预算内比较谁更划算，而不是给所有幸存者都打 100 分。
     */
    private List<Combo> applyHardFilter(RecommendRequest request, List<Combo> candidates) {
        final BigDecimal min = request.getBudgetMin();
        final BigDecimal max = request.getBudgetMax();
        final CalorieLevel level = CalorieLevel.of(request.getCalorieLevel());
        final Integer maxCalories = request.getMaxCalories();
        final int threshold = properties.getCalorieThreshold();

        return candidates.stream().filter(c -> {
            // ① 预算区间（闭区间）
            BigDecimal price = c.getPrice();
            if (price != null) {
                if (min != null && price.compareTo(min) < 0) {
                    return false;
                }
                if (max != null && price.compareTo(max) > 0) {
                    return false;
                }
            }
            // ② 热量档位：偏低 = ≤ 阈值，偏高 = > 阈值
            Integer calories = c.getCalories();
            if (calories != null && level != null) {
                if (level == CalorieLevel.LOW && calories > threshold) {
                    return false;
                }
                if (level == CalorieLevel.HIGH && calories <= threshold) {
                    return false;
                }
            }
            // ③ 精确热量上限（API 直接给数值时）
            if (calories != null && maxCalories != null && calories > maxCalories) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 把「用户设了哪些硬性条件」拼成一段可读文字，用于摘要与空结果提示
     */
    private String describeConditions(RecommendRequest request) {
        List<String> conds = new ArrayList<>();
        if (request.getBudgetMax() != null) {
            BigDecimal min = request.getBudgetMin();
            String range = min == null
                    ? "¥" + fmt(request.getBudgetMax()) + " 以内"
                    : "¥" + fmt(min) + "-¥" + fmt(request.getBudgetMax());
            conds.add("预算 " + range);
        }
        CalorieLevel level = CalorieLevel.of(request.getCalorieLevel());
        if (level != null) {
            conds.add(level.getDesc() + "（以 " + properties.getCalorieThreshold() + " kcal 为界）");
        }
        if (request.getMaxCalories() != null) {
            conds.add("热量 ≤ " + request.getMaxCalories() + " kcal");
        }
        return String.join("、", conds);
    }

    /**
     * 生成一句话推荐总结
     */
    private String buildSummary(RecommendRequest request, TimeSlot timeSlot, List<RecommendItemVO> items) {
        String condText = describeConditions(request);
        if (items.isEmpty()) {
            StringBuilder empty = new StringBuilder("没有找到同时满足这些条件的套餐");
            if (!condText.isEmpty()) {
                empty.append("（").append(condText).append("）");
            }
            empty.append("，试着放宽预算范围或换个热量档位吧");
            return empty.toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("当前是「").append(timeSlot.getDesc()).append("」时段，");
        if (!condText.isEmpty()) {
            sb.append(condText).append("，");
        }
        if (request.getPeopleCount() != null) {
            int adults = request.getPeopleCount();
            int children = request.getChildCount() == null ? 0 : Math.max(0, request.getChildCount());
            if (children > 0) {
                double eff = adults + children * PeopleFitStrategy.CHILD_FACTOR;
                String effText = eff == Math.rint(eff)
                        ? String.valueOf((int) eff)
                        : String.format("%.1f", eff);
                sb.append(adults).append(" 大人 + ").append(children)
                        .append(" 儿童（约 ").append(effText).append(" 人份）用餐，");
            } else {
                sb.append(adults).append(" 人用餐，");
            }
        }
        RecommendItemVO top = items.get(0);
        sb.append("为你精选 ").append(items.size()).append(" 个套餐，最推荐「")
                .append(top.getCombo() == null ? "" : top.getCombo().getName())
                .append("」，最高综合得分 ").append(top.getScore()).append(" 分。");
        return sb.toString();
    }

    /**
     * 推荐记录落库
     */
    private void saveRecord(RecommendRequest request, RecommendResponse response) {
        try {
            RecommendRecord record = new RecommendRecord();
            record.setUserId(StringUtils.hasText(request.getUserId()) ? request.getUserId() : "anonymous");
            record.setRequestJson(objectMapper.writeValueAsString(request));
            record.setResultJson(objectMapper.writeValueAsString(response));
            if (!response.getItems().isEmpty()) {
                RecommendItemVO top = response.getItems().get(0);
                record.setTopComboId(top.getCombo() == null ? null : top.getCombo().getId());
                record.setTopScore(top.getScore());
            }
            record.setCostMs(response.getCostMs());
            recommendRecordMapper.insert(record);
        } catch (Exception e) {
            log.warn("推荐记录落库失败: {}", e.getMessage());
        }
    }

    private static String fmt(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
