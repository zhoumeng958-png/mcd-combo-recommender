package com.mcd.recommender.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcd.recommender.entity.Combo;
import org.apache.ibatis.annotations.Mapper;

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
}
