package com.mcd.recommender.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcd.recommender.entity.RecommendRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推荐记录 Mapper
 */
@Mapper
public interface RecommendRecordMapper extends BaseMapper<RecommendRecord> {
}
