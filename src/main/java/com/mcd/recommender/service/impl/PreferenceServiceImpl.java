package com.mcd.recommender.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.ResultCode;
import com.mcd.recommender.entity.Preference;
import com.mcd.recommender.mapper.PreferenceMapper;
import com.mcd.recommender.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户偏好服务实现
 */
@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceMapper preferenceMapper;

    @Override
    public Preference getByUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "userId 不能为空");
        }
        return preferenceMapper.selectOne(new LambdaQueryWrapper<Preference>()
                .eq(Preference::getUserId, userId)
                .last("limit 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Preference saveOrUpdate(Preference preference) {
        if (!StringUtils.hasText(preference.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "userId 不能为空");
        }
        Preference exist = preferenceMapper.selectOne(new LambdaQueryWrapper<Preference>()
                .eq(Preference::getUserId, preference.getUserId())
                .last("limit 1"));
        if (exist == null) {
            preferenceMapper.insert(preference);
            return preference;
        }
        preference.setId(exist.getId());
        preferenceMapper.updateById(preference);
        return preferenceMapper.selectById(exist.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (preferenceMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.PREFERENCE_NOT_FOUND);
        }
        preferenceMapper.deleteById(id);
    }
}
