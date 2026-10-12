package com.mcd.recommender.controller;

import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.common.Result;
import com.mcd.recommender.entity.Product;
import com.mcd.recommender.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 单品管理接口
 */
@Tag(name = "03 - 单品管理", description = "汉堡 / 小食 / 饮料 / 甜品 / 早餐单品的查询与维护")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询单品")
    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @Parameter(description = "名称关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类：HAMBURGER/SIDE/DRINK/DESSERT/BREAKFAST") @RequestParam(required = false) String category,
            @Parameter(description = "页码") @RequestParam(required = false) Long current,
            @Parameter(description = "每页条数") @RequestParam(required = false) Long size) {
        return Result.success(productService.page(keyword, category, current, size));
    }

    @Operation(summary = "查询单品详情")
    @GetMapping("/{id}")
    public Result<Product> detail(@Parameter(description = "单品ID") @PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    @Operation(summary = "按分类查询单品")
    @GetMapping("/list")
    public Result<List<Product>> list(
            @Parameter(description = "分类，不传返回全部") @RequestParam(required = false) String category) {
        return Result.success(productService.listByCategory(category));
    }

    @Operation(summary = "新增单品")
    @PostMapping
    public Result<Long> create(@RequestBody Product product) {
        return Result.success("新增成功", productService.create(product));
    }

    @Operation(summary = "更新单品")
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除单品")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@Parameter(description = "单品ID") @PathVariable Long id) {
        productService.remove(id);
        return Result.success("删除成功", null);
    }
}
