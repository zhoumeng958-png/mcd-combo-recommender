package com.mcd.recommender.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.common.ResultCode;
import com.mcd.recommender.entity.Product;
import com.mcd.recommender.mapper.ProductMapper;
import com.mcd.recommender.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 单品服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public PageResult<Product> page(String keyword, String category, Long current, Long size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword.trim());
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category.trim().toUpperCase());
        }
        wrapper.orderByAsc(Product::getCategory).orderByAsc(Product::getId);

        Page<Product> page = productMapper.selectPage(
                new Page<>(current == null || current < 1 ? 1L : current,
                        size == null || size < 1 ? 10L : size),
                wrapper);
        return PageResult.of(page);
    }

    @Override
    public Product detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    @Override
    public List<Product> listByCategory(String category) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category.trim().toUpperCase());
        }
        wrapper.orderByAsc(Product::getId);
        return productMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Product product) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Product product) {
        if (product.getId() == null) {
            throw new BusinessException("单品ID不能为空");
        }
        if (productMapper.selectById(product.getId()) == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (productMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        productMapper.deleteById(id);
    }
}
