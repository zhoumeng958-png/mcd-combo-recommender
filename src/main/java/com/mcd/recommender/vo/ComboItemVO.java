package com.mcd.recommender.vo;

import com.mcd.recommender.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 套餐内单品明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "套餐内单品")
public class ComboItemVO extends Product {

    private static final long serialVersionUID = 1L;

    @Schema(description = "在套餐中的数量")
    private Integer quantity;
}
