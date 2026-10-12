package com.mcd.recommender.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcd.recommender.entity.ComboItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 套餐明细 Mapper
 */
@Mapper
public interface ComboItemMapper extends BaseMapper<ComboItem> {
}
