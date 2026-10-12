package com.mcd.recommender.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcd.recommender.entity.Preference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好 Mapper
 */
@Mapper
public interface PreferenceMapper extends BaseMapper<Preference> {
}
