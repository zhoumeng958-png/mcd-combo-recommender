package com.mcd.recommender.service;

import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.RecommendRecord;
import com.mcd.recommender.vo.RecommendResponse;

import java.util.List;

/**
 * 套餐推荐服务
 */
public interface RecommendService {

    /**
     * 根据用户偏好推荐套餐
     */
    RecommendResponse recommend(RecommendRequest request);

    /**
     * 查询最近的推荐记录
     */
    List<RecommendRecord> recentRecords(String userId, Integer limit);
}
