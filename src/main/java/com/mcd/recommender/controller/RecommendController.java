package com.mcd.recommender.controller;

import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.Result;
import com.mcd.recommender.common.ResultCode;
import com.mcd.recommender.dto.RecommendRequest;
import com.mcd.recommender.entity.Preference;
import com.mcd.recommender.entity.RecommendRecord;
import com.mcd.recommender.security.UserContext;
import com.mcd.recommender.service.PreferenceService;
import com.mcd.recommender.service.RecommendService;
import com.mcd.recommender.util.TagUtils;
import com.mcd.recommender.vo.RecommendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐推荐接口
 */
@Tag(name = "01 - 套餐推荐", description = "核心推荐接口：传入偏好，返回带推荐理由的套餐榜单")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final PreferenceService preferenceService;

    @Operation(summary = "智能推荐套餐",
            description = "按预算 / 口味 / 人数 / 时段等条件，返回带推荐理由的套餐榜单。userId 自动取自登录态")
    @PostMapping
    public Result<RecommendResponse> recommend(@Valid @RequestBody RecommendRequest request) {
        // 用户标识一律取自登录态，前端无需传也无法伪造
        request.setUserId(UserContext.currentUsername());
        return Result.success("推荐成功", recommendService.recommend(request));
    }

    @Operation(summary = "快速推荐（GET）", description = "便于浏览器直接调试的简化版推荐接口")
    @GetMapping("/quick")
    public Result<RecommendResponse> quickRecommend(
            @Parameter(description = "预算上限（元）") @RequestParam(required = false) BigDecimal budgetMax,
            @Parameter(description = "预算下限（元）") @RequestParam(required = false) BigDecimal budgetMin,
            @Parameter(description = "偏好口味，逗号分隔，如：经典,牛肉") @RequestParam(required = false) String tasteTags,
            @Parameter(description = "忌口标签，逗号分隔，如：香辣") @RequestParam(required = false) String avoidTags,
            @Parameter(description = "就餐人数") @RequestParam(required = false) Integer peopleCount,
            @Parameter(description = "时段：BREAKFAST/LUNCH/TEA/DINNER/LATE_NIGHT") @RequestParam(required = false) String timeSlot,
            @Parameter(description = "热量偏好：LOW=热量偏低 / HIGH=热量偏高，不传=不限") @RequestParam(required = false) String calorieLevel,
            @Parameter(description = "最多返回几条，不传取配置默认值") @RequestParam(required = false) Integer topN) {

        RecommendRequest request = new RecommendRequest();
        request.setUserId("quick_query");
        request.setBudgetMax(budgetMax);
        request.setBudgetMin(budgetMin);
        request.setTasteTags(TagUtils.split(tasteTags));
        request.setAvoidTags(TagUtils.split(avoidTags));
        request.setPeopleCount(peopleCount);
        request.setTimeSlot(timeSlot);
        request.setCalorieLevel(calorieLevel);
        request.setTopN(topN);
        return Result.success("推荐成功", recommendService.recommend(request));
    }

    @Operation(summary = "按我保存的偏好一键推荐",
            description = "读取当前登录用户的偏好配置，自动生成推荐结果")
    @GetMapping("/by-preference")
    public Result<RecommendResponse> recommendByPreference() {

        String username = UserContext.currentUsername();
        Preference preference = preferenceService.getByUserId(username);
        if (preference == null) {
            throw new BusinessException(ResultCode.PREFERENCE_NOT_FOUND, "你还没有保存偏好配置");
        }

        RecommendRequest request = new RecommendRequest();
        request.setUserId(username);
        request.setBudgetMin(preference.getBudgetMin());
        request.setBudgetMax(preference.getBudgetMax());
        request.setTasteTags(TagUtils.split(preference.getTasteTags()));
        request.setAvoidTags(TagUtils.split(preference.getAvoidTags()));
        request.setPeopleCount(preference.getPeopleCount());
        request.setTimeSlot(StringUtils.hasText(preference.getTimeSlot())
                ? preference.getTimeSlot() : null);
        request.setPreferredCategory(preference.getPreferredCategory());
        return Result.success("推荐成功", recommendService.recommend(request));
    }

    @Operation(summary = "查询推荐历史记录",
            description = "普通用户只能查到自己的记录；管理员传 all=true 可查全部")
    @GetMapping("/records")
    public Result<List<RecommendRecord>> records(
            @Parameter(description = "仅管理员可用：传 true 查询全部用户记录")
            @RequestParam(required = false) Boolean all,
            @Parameter(description = "返回条数，默认 10") @RequestParam(required = false) Integer limit) {

        // 默认只看自己的记录，避免越权查看他人数据
        String targetUser = Boolean.TRUE.equals(all) && UserContext.isAdmin()
                ? null
                : UserContext.currentUsername();
        return Result.success(recommendService.recentRecords(targetUser, limit));
    }
}
