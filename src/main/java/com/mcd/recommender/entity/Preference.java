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
 * 用户口味偏好
 */
@Data
@TableName("mcd_preference")
public class Preference implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户标识 */
    private String userId;

    /** 预算下限 */
    private BigDecimal budgetMin;

    /** 预算上限 */
    private BigDecimal budgetMax;

    /** 偏好口味，逗号分隔 */
    private String tasteTags;

    /** 忌口标签，逗号分隔 */
    private String avoidTags;

    /** 就餐人数 */
    private Integer peopleCount;

    /** 常用时段 */
    private String timeSlot;

    /** 偏好的套餐分类 */
    private String preferredCategory;

    /** 备注 */
    private String remark;

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
