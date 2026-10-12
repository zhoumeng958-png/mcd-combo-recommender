package com.mcd.recommender.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐推荐请求参数
 */
@Data
@Schema(description = "套餐推荐请求参数")
public class RecommendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户标识", hidden = true,
            accessMode = Schema.AccessMode.READ_ONLY)
    private String userId;

    @DecimalMin(value = "0.0", message = "预算下限不能为负数")
    @Schema(description = "预算下限（元）", example = "30")
    private BigDecimal budgetMin;

    @DecimalMin(value = "0.0", message = "预算上限不能为负数")
    @Schema(description = "预算上限（元）", example = "60")
    private BigDecimal budgetMax;

    @Schema(description = "偏好口味标签，命中越多分越高", example = "[\"经典\",\"牛肉\"]")
    private List<String> tasteTags;

    @Schema(description = "忌口标签，命中则直接扣分/排除", example = "[\"香辣\"]")
    private List<String> avoidTags;

    @Min(value = 1, message = "就餐人数至少 1 人")
    @Max(value = 20, message = "就餐人数最多 20 人")
    @Schema(description = "就餐人数", example = "1")
    private Integer peopleCount;

    @Schema(description = "就餐时段，不传则按当前时间自动推断",
            allowableValues = {"BREAKFAST", "LUNCH", "TEA", "DINNER", "LATE_NIGHT", "ALL_DAY"})
    private String timeSlot;

    @Schema(description = "偏好的套餐分类",
            allowableValues = {"CLASSIC", "SPICY", "VALUE", "FAMILY", "BREAKFAST", "TEA", "DESSERT"})
    private String preferredCategory;

    @Min(value = 0, message = "辣度取值为 0-3")
    @Max(value = 3, message = "辣度取值为 0-3")
    @Schema(description = "期望辣度 0-3，0 表示不吃辣", example = "0")
    private Integer spicyLevel;

    @Schema(description = "单份热量上限 kcal（减脂场景可用）", example = "1200")
    private Integer maxCalories;

    @Min(value = 1, message = "返回数量至少 1")
    @Max(value = 20, message = "返回数量最多 20")
    @Schema(description = "期望返回的推荐数量，不传取配置默认值", example = "5")
    private Integer topN;
}
