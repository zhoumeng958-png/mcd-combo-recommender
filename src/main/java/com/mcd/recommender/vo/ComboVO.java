package com.mcd.recommender.vo;

import com.mcd.recommender.entity.Combo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐展示对象（套餐信息 + 包含的单品明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "套餐详情")
public class ComboVO extends Combo {

    private static final long serialVersionUID = 1L;

    @Schema(description = "套餐包含的单品明细")
    private List<ComboItemVO> items = new ArrayList<>();

    @Schema(description = "相比单点可省金额（元）")
    private BigDecimal saveAmount;

    @Schema(description = "折扣，如 8.9 表示 8.9 折")
    private BigDecimal discount;

    @Schema(description = "口味标签列表")
    private List<String> tasteTagList = new ArrayList<>();
}
