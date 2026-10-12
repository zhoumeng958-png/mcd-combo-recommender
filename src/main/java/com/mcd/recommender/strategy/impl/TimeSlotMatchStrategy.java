package com.mcd.recommender.strategy.impl;

import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.enums.TimeSlot;
import com.mcd.recommender.strategy.RecommendContext;
import com.mcd.recommender.strategy.RecommendStrategy;
import com.mcd.recommender.util.TagUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 时段适配策略
 * <p>
 * 早餐时段推早餐组合，夜宵时段推解馋套餐；全天供应的套餐给「保底高分」，
 * 保证任何时候都有得推，但优先推真正应景的。
 */
@Component
public class TimeSlotMatchStrategy implements RecommendStrategy {

    /** 全天供应套餐的保底分 */
    private static final double ALL_DAY_SCORE = 90D;

    /** 时段完全不匹配的分数 */
    private static final double MISMATCH_SCORE = 25D;

    @Override
    public String name() {
        return "timeSlot";
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public boolean score(RecommendContext context) {
        TimeSlot slot = context.getEffectiveTimeSlot();
        if (slot == null) {
            return false;
        }

        for (Combo combo : context.getCandidates()) {
            List<String> slots = TagUtils.split(combo.getTimeSlots());
            double score;
            if (slots.contains(slot.name())) {
                score = 100D;
                context.addReason(combo.getId(), String.format("专为「%s」时段准备", slot.getDesc()));
            } else if (slots.contains(TimeSlot.ALL_DAY.name()) || slots.isEmpty()) {
                score = ALL_DAY_SCORE;
            } else {
                score = MISMATCH_SCORE;
                context.addReason(combo.getId(), String.format("更适合其他时段，当前是「%s」", slot.getDesc()));
            }
            context.addScore(combo.getId(), name(), score);
        }
        return true;
    }
}
