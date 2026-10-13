package com.mcd.recommender.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.ResultCode;
import com.mcd.recommender.config.RecommenderProperties;
import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.entity.RecommendRecord;
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
        candidates = applyHardFilter(request, candidates);
        if (candidates.isEmpty()) {
            throw new BusinessException(ResultCode.RECOMMEND_NO_RESULT, "没有符合热量要求的套餐，请放宽条件");
        }

        // 4. 批量装配 VO（一次查明细，避免 N+1）
        //    提前到打分之前：策略需要知道套餐「内容」（例如是否含甜品），
        //    而且取完 TopN 后还能复用这份结果，不额外增加查询。
        Map<Long, ComboVO> comboVoMap = comboService.toVOList(candidates).stream()
                .collect(Collectors.toMap(ComboVO::getId, Function.identity(), (a, b) -> a));

        // 5. 策略引擎打分
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
        List<ScoredCombo> scoredList = strategyEngine.recommend(context);

        // 6. 截取 TopN
        int topN = request.getTopN() != null ? request.getTopN() : properties.getTopN();
        topN = Math.min(Math.max(topN, 1), scoredList.size());
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
     * 硬性过滤：热量上限
     */
    private List<Combo> applyHardFilter(RecommendRequest request, List<Combo> candidates) {
        if (request.getMaxCalories() == null) {
            return candidates;
        }
        final int maxCalories = request.getMaxCalories();
        return candidates.stream()
                .filter(c -> c.getCalories() == null || c.getCalories() <= maxCalories)
                .collect(Collectors.toList());
    }

    /**
     * 生成一句话推荐总结
     */
    private String buildSummary(RecommendRequest request, TimeSlot timeSlot, List<RecommendItemVO> items) {
        if (items.isEmpty()) {
            return "暂时没有找到合适的套餐，试着放宽预算或口味条件吧";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("当前是「").append(timeSlot.getDesc()).append("」时段，");
        if (request.getBudgetMax() != null) {
            sb.append("预算 ");
            if (request.getBudgetMin() != null) {
                sb.append("¥").append(fmt(request.getBudgetMin())).append("-");
            } else {
                sb.append("¥");
            }
            sb.append("¥").append(fmt(request.getBudgetMax())).append("，");
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
