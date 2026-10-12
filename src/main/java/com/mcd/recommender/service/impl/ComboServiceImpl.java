package com.mcd.recommender.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.common.ResultCode;
import com.mcd.recommender.dto.ComboQuery;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.entity.ComboItem;
import com.mcd.recommender.entity.Product;
import com.mcd.recommender.mapper.ComboItemMapper;
import com.mcd.recommender.mapper.ComboMapper;
import com.mcd.recommender.mapper.ProductMapper;
import com.mcd.recommender.service.ComboService;
import com.mcd.recommender.util.TagUtils;
import com.mcd.recommender.vo.ComboItemVO;
import com.mcd.recommender.vo.ComboVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 套餐服务实现
 */
@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboMapper comboMapper;
    private final ComboItemMapper comboItemMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult<ComboVO> page(ComboQuery query) {
        LambdaQueryWrapper<Combo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Combo::getStatus, 1);

        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Combo::getName, keyword).or().like(Combo::getCode, keyword));
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Combo::getCategory, query.getCategory().trim().toUpperCase());
        }
        if (query.getMinPrice() != null) {
            wrapper.ge(Combo::getPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Combo::getPrice, query.getMaxPrice());
        }
        if (query.getPeopleCount() != null) {
            wrapper.eq(Combo::getPeopleCount, query.getPeopleCount());
        }

        boolean asc = "asc".equalsIgnoreCase(query.getSortOrder());
        String sortField = query.getSortField();
        if (StringUtils.hasText(sortField)) {
            switch (sortField) {
                case "price":
                    wrapper.orderBy(true, asc, Combo::getPrice);
                    break;
                case "rating":
                    wrapper.orderBy(true, asc, Combo::getRating);
                    break;
                case "calories":
                    wrapper.orderBy(true, asc, Combo::getCalories);
                    break;
                case "salesCount":
                default:
                    wrapper.orderBy(true, asc, Combo::getSalesCount);
                    break;
            }
        } else {
            wrapper.orderByDesc(Combo::getSalesCount);
        }

        Page<Combo> page = comboMapper.selectPage(
                new Page<>(query.getCurrent() == null || query.getCurrent() < 1 ? 1L : query.getCurrent(),
                        query.getSize() == null || query.getSize() < 1 ? 10L : query.getSize()),
                wrapper);

        PageResult<ComboVO> result = new PageResult<>();
        result.setRecords(toVOList(page.getRecords()));
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    @Override
    public ComboVO detail(Long id) {
        Combo combo = comboMapper.selectById(id);
        if (combo == null) {
            throw new BusinessException(ResultCode.COMBO_NOT_FOUND);
        }
        return toVOList(Collections.singletonList(combo)).get(0);
    }

    @Override
    public List<ComboVO> listAll() {
        LambdaQueryWrapper<Combo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Combo::getStatus, 1).orderByDesc(Combo::getSalesCount);
        return toVOList(comboMapper.selectList(wrapper));
    }

    /**
     * 批量转换，明细与单品均批量查询，避免 N+1
     */
    @Override
    public List<ComboVO> toVOList(List<Combo> combos) {
        if (CollectionUtils.isEmpty(combos)) {
            return new ArrayList<>();
        }
        List<Long> comboIds = combos.stream().map(Combo::getId).collect(Collectors.toList());

        // 一次性查出所有套餐明细
        List<ComboItem> comboItems = comboItemMapper.selectList(
                new LambdaQueryWrapper<ComboItem>().in(ComboItem::getComboId, comboIds));
        Map<Long, List<ComboItem>> itemGroup = comboItems.stream()
                .collect(Collectors.groupingBy(ComboItem::getComboId));

        // 一次性查出涉及的所有单品
        Set<Long> productIds = comboItems.stream()
                .map(ComboItem::getProductId)
                .collect(Collectors.toSet());
        Map<Long, Product> productMap = productIds.isEmpty()
                ? Collections.emptyMap()
                : productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        List<ComboVO> result = new ArrayList<>(combos.size());
        for (Combo combo : combos) {
            ComboVO vo = new ComboVO();
            BeanUtils.copyProperties(combo, vo);

            List<ComboItemVO> itemVOs = new ArrayList<>();
            for (ComboItem item : itemGroup.getOrDefault(combo.getId(), Collections.emptyList())) {
                Product product = productMap.get(item.getProductId());
                if (product == null) {
                    continue;
                }
                ComboItemVO itemVO = new ComboItemVO();
                BeanUtils.copyProperties(product, itemVO);
                itemVO.setQuantity(item.getQuantity() == null ? 1 : item.getQuantity());
                itemVOs.add(itemVO);
            }
            vo.setItems(itemVOs);
            vo.setTasteTagList(TagUtils.split(combo.getTasteTags()));
            fillDiscount(vo);
            result.add(vo);
        }
        return result;
    }

    /**
     * 计算「立省金额」与「折扣」
     */
    private void fillDiscount(ComboVO vo) {
        BigDecimal original = vo.getOriginalPrice();
        BigDecimal price = vo.getPrice();
        if (original == null || price == null || original.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        vo.setSaveAmount(original.subtract(price).setScale(2, RoundingMode.HALF_UP));
        vo.setDiscount(price.multiply(BigDecimal.TEN)
                .divide(original, 1, RoundingMode.HALF_UP));
    }

    @Override
    public List<Map<String, Object>> categoryStat() {
        List<Map<String, Object>> raw = comboMapper.selectCategoryStat();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            Map<String, Object> item = new LinkedHashMap<>(row);
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Combo combo) {
        if (combo.getStatus() == null) {
            combo.setStatus(1);
        }
        comboMapper.insert(combo);
        return combo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Combo combo) {
        if (combo.getId() == null) {
            throw new BusinessException("套餐ID不能为空");
        }
        if (comboMapper.selectById(combo.getId()) == null) {
            throw new BusinessException(ResultCode.COMBO_NOT_FOUND);
        }
        comboMapper.updateById(combo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (comboMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.COMBO_NOT_FOUND);
        }
        comboMapper.deleteById(id);
        // 同步删除明细
        comboItemMapper.delete(new LambdaQueryWrapper<ComboItem>().eq(ComboItem::getComboId, id));
    }
}
