package com.mcd.recommender.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐
 */
@Data
@TableName("mcd_combo")
public class Combo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐编码 */
    private String code;

    /** 套餐名称 */
    private String name;

    /** 分类：CLASSIC/SPICY/VALUE/FAMILY/BREAKFAST/TEA/DESSERT */
    private String category;

    /** 套餐价 */
    private BigDecimal price;

    /** 单点合计原价 */
    private BigDecimal originalPrice;

    /** 建议就餐人数 */
    private Integer peopleCount;

    /** 套餐总热量 kcal */
    private Integer calories;

    /** 辣度 0-3 */
    private Integer spicyLevel;

    /** 口味标签，逗号分隔 */
    private String tasteTags;

    /** 适用时段，逗号分隔 */
    private String timeSlots;

    /** 营销标签，逗号分隔 */
    private String tags;

    /** 累计销量，用于人气打分 */
    private Integer salesCount;

    /** 评分 0-5 */
    private BigDecimal rating;

    /** 套餐描述 */
    private String description;

    /** 图片地址 */
    private String imageUrl;

    /** 状态 1上架 0下架 */
    private Integer status;

    @TableLogic
    @TableField(select = false)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
