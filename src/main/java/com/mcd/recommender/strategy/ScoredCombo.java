package com.mcd.recommender.strategy;

import com.mcd.recommender.entity.Combo;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 打分后的套餐（引擎输出）
 */
@Data
public class ScoredCombo {

    /** 套餐 */
    private Combo combo;

    /** 加权综合得分 0-100 */
    private double score;

    /** 推荐理由 */
    private List<String> reasons = new ArrayList<>();

    /** 各策略单项得分明细 */
    private Map<String, Double> scoreDetail = new LinkedHashMap<>();

    public ScoredCombo(Combo combo) {
        this.combo = combo;
    }
}
