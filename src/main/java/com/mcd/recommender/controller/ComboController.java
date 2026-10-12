package com.mcd.recommender.controller;

import com.mcd.recommender.common.PageResult;
import com.mcd.recommender.common.Result;
import com.mcd.recommender.dto.ComboQuery;
import com.mcd.recommender.entity.Combo;
import com.mcd.recommender.service.ComboService;
import com.mcd.recommender.vo.ComboVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 套餐管理接口
 */
@Tag(name = "02 - 套餐管理", description = "套餐的查询与维护")
@RestController
@RequestMapping("/api/combo")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @Operation(summary = "分页查询套餐", description = "支持关键词、分类、价格区间、人数筛选与排序")
    @GetMapping("/page")
    public Result<PageResult<ComboVO>> page(ComboQuery query) {
        return Result.success(comboService.page(query));
    }

    @Operation(summary = "查询套餐详情", description = "返回套餐信息 + 包含的单品明细 + 立省金额与折扣")
    @GetMapping("/{id}")
    public Result<ComboVO> detail(@Parameter(description = "套餐ID") @PathVariable Long id) {
        return Result.success(comboService.detail(id));
    }

    @Operation(summary = "查询全部上架套餐")
    @GetMapping("/list")
    public Result<List<ComboVO>> list() {
        return Result.success(comboService.listAll());
    }

    @Operation(summary = "套餐分类统计", description = "按分类统计数量、均价、平均热量与总销量")
    @GetMapping("/stat")
    public Result<List<Map<String, Object>>> stat() {
        return Result.success(comboService.categoryStat());
    }

    @Operation(summary = "新增套餐")
    @PostMapping
    public Result<Long> create(@RequestBody Combo combo) {
        return Result.success("新增成功", comboService.create(combo));
    }

    @Operation(summary = "更新套餐")
    @PutMapping
    public Result<Void> update(@RequestBody Combo combo) {
        comboService.update(combo);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除套餐", description = "逻辑删除，同时删除套餐明细")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@Parameter(description = "套餐ID") @PathVariable Long id) {
        comboService.remove(id);
        return Result.success("删除成功", null);
    }
}
