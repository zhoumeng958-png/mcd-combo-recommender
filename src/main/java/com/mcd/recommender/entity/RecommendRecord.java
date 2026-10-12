package com.mcd.recommender.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐记录（留痕，便于复盘与演示）
 */
@Data
@TableName("mcd_recommend_record")
public class RecommendRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户标识 */
    private String userId;

    /** 请求参数快照（JSON） */
    private String requestJson;

    /** 推荐结果快照（JSON） */
    private String resultJson;

    /** Top1 套餐ID */
    private Long topComboId;

    /** Top1 得分 */
    private BigDecimal topScore;

    /** 耗时 ms */
    private Long costMs;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
