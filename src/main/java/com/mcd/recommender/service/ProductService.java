package com.mcd.recommender.service;

import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.entity.Product;

import java.util.List;

/**
 * 单品服务
 */
public interface ProductService {

    /** 分页查询单品 */
    PageResult<Product> page(String keyword, String category, Long current, Long size);

    /** 查询单品详情 */
    Product detail(Long id);

    /** 按分类查询单品 */
    List<Product> listByCategory(String category);

    /** 新增单品 */
    Long create(Product product);

    /** 更新单品 */
    void update(Product product);

    /** 删除单品（逻辑删除） */
    void remove(Long id);
}
