package com.mcd.recommender.service;

import com.mcd.recommender.entity.Preference;

/**
 * 用户偏好服务
 */
public interface PreferenceService {

    /** 按用户标识查询偏好 */
    Preference getByUserId(String userId);

    /** 保存或更新偏好（按 userId 唯一） */
    Preference saveOrUpdate(Preference preference);

    /** 删除偏好 */
    void remove(Long id);
}
