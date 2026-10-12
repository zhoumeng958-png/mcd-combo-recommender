package com.mcd.recommender.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcd.recommender.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 单品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
