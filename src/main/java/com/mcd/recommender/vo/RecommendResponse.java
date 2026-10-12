package com.mcd.recommender.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 推荐接口响应
 */
@Data
@Schema(description = "推荐结果响应")
public class RecommendResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户标识")
    private String userId;

    @Schema(description = "实际使用的就餐时段")
    private String timeSlot;

    @Schema(description = "参与打分的候选套餐数量")
    private Integer totalCandidates;

    @Schema(description = "推荐计算耗时（毫秒）")
    private Long costMs;

    @Schema(description = "推荐结果列表，按得分从高到低")
    private List<RecommendItemVO> items = new ArrayList<>();

    @Schema(description = "一句话推荐总结")
    private String summary;
}
