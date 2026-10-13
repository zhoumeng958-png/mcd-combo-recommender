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
    @Schema(description = "预算下限（元）。与 budgetMax 一起构成**硬边界**："
            + "价格不在区间内的套餐在打分之前就被淘汰，不会出现在结果里", example = "30")
    private BigDecimal budgetMin;

    @DecimalMin(value = "0.0", message = "预算上限不能为负数")
    @Schema(description = "预算上限（元）。见 budgetMin 的说明", example = "60")
    private BigDecimal budgetMax;

    @Schema(description = "偏好口味标签，命中越多分越高", example = "[\"经典\",\"牛肉\"]")
    private List<String> tasteTags;

    @Schema(description = "忌口标签，命中则直接扣分/排除", example = "[\"香辣\"]")
    private List<String> avoidTags;

    @Min(value = 1, message = "就餐人数至少 1 人")
    @Max(value = 20, message = "就餐人数最多 20 人")
    @Schema(description = "就餐的大人人数", example = "2")
    private Integer peopleCount;

    @Min(value = 0, message = "儿童人数不能为负数")
    @Max(value = 10, message = "儿童人数最多 10 人")
    @Schema(description = "就餐的儿童人数，儿童饭量按 0.6 个大人折算", example = "1")
    private Integer childCount;

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

    @Schema(description = "热量偏好档位：LOW=热量偏低 / HIGH=热量偏高，不传=不限。"
            + "分界线由 recommender.calorie-threshold 配置（默认 1000 kcal）",
            allowableValues = {"LOW", "HIGH"}, example = "LOW")
    private String calorieLevel;

    @Schema(description = "单份热量上限 kcal（精确控制时用，一般走 calorieLevel 即可）", example = "1200")
    private Integer maxCalories;

    @Min(value = 1, message = "返回数量至少 1")
    @Max(value = 20, message = "返回数量最多 20")
    @Schema(description = "最多返回几条。一般不用传，默认取配置 recommender.top-n；"
            + "实际返回数不会超过符合条件的套餐数", example = "5")
    private Integer topN;
}
