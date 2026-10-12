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
 * 单品（汉堡 / 小食 / 饮料 / 甜品 / 早餐）
 */
@Data
@TableName("mcd_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 单品名称 */
    private String name;

    /** 分类：HAMBURGER/SIDE/DRINK/DESSERT/BREAKFAST */
    private String category;

    /** 单点价格 */
    private BigDecimal price;

    /** 热量 kcal */
    private Integer calories;

    /** 辣度 0-3 */
    private Integer spicyLevel;

    /** 口味标签，逗号分隔 */
    private String tasteTags;

    /** 图片地址 */
    private String imageUrl;

    /** 描述 */
    private String description;

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
