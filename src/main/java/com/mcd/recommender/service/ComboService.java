package com.mcd.recommender.service;

import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.dto.ComboQuery;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.vo.ComboVO;

import java.util.List;
import java.util.Map;

/**
 * 套餐服务
 */
public interface ComboService {

    /** 分页查询套餐（含单品明细） */
    PageResult<ComboVO> page(ComboQuery query);

    /** 查询套餐详情 */
    ComboVO detail(Long id);

    /** 查询全部上架套餐（含明细） */
    List<ComboVO> listAll();

    /** 批量把实体转换为 VO（含明细，已做批量查询避免 N+1） */
    List<ComboVO> toVOList(List<Combo> combos);

    /** 按分类统计 */
    List<Map<String, Object>> categoryStat();

    /** 新增套餐 */
    Long create(Combo combo);

    /** 更新套餐 */
    void update(Combo combo);

    /** 删除套餐（逻辑删除） */
    void remove(Long id);
}
