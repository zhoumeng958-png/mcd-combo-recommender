package com.mcd.recommender.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcd.recommender.entity.Combo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 套餐 Mapper
 */
@Mapper
public interface ComboMapper extends BaseMapper<Combo> {

    /**
     * 按套餐分类统计数量、平均价格、总销量（XML 写法示例）
     */
    List<Map<String, Object>> selectCategoryStat();

    /**
     * 关键词命中「单品名称」时，反查包含这些单品的套餐 ID
     * （例如搜「薯条」→ 所有含中/大/小薯条的套餐）
     *
     * @param keyword 单品名称关键词，为空时返回空集合
     */
    List<Long> selectComboIdsByProductName(@Param("keyword") String keyword);
}
