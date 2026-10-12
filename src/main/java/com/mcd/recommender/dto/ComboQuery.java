package com.mcd.recommender.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐分页查询条件
 */
@Data
@Schema(description = "套餐分页查询条件")
public class ComboQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词，匹配套餐名称或编码")
    private String keyword;

    @Schema(description = "套餐分类")
    private String category;

    @Schema(description = "价格下限")
    private BigDecimal minPrice;

    @Schema(description = "价格上限")
    private BigDecimal maxPrice;

    @Schema(description = "建议人数")
    private Integer peopleCount;

    @Schema(description = "排序字段：price / salesCount / rating / calories", example = "salesCount")
    private String sortField;

    @Schema(description = "排序方向：asc / desc", example = "desc")
    private String sortOrder;

    @Schema(description = "页码，从 1 开始", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;
}
