package com.mcd.recommender.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单条推荐结果
 */
@Data
@Schema(description = "单条推荐结果")
public class RecommendItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "排名，从 1 开始")
    private Integer rank;

    @Schema(description = "套餐详情")
    private ComboVO combo;

    @Schema(description = "综合得分（0-100）")
    private BigDecimal score;

    @Schema(description = "推荐理由（人类可读）")
    private List<String> reasons = new ArrayList<>();

    @Schema(description = "各策略得分明细，key 为策略名")
    private Map<String, BigDecimal> scoreDetail = new LinkedHashMap<>();
}
