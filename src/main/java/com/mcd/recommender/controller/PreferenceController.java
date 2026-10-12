package com.mcd.recommender.controller;

import com.mcd.recommender.common.Result;
import com.mcd.recommender.entity.Preference;
import com.mcd.recommender.security.UserContext;
import com.mcd.recommender.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户偏好接口
 * <p>
 * 所有操作都绑定当前登录用户，不再通过参数传 userId，
 * 从根上避免了「改别人偏好」的越权问题。
 */
@Tag(name = "04 - 用户偏好", description = "当前登录用户的口味偏好，保存后可一键推荐")
@RestController
@RequestMapping("/api/preference")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @Operation(summary = "查询我的偏好")
    @GetMapping
    public Result<Preference> get() {
        return Result.success(preferenceService.getByUserId(UserContext.currentUsername()));
    }

    @Operation(summary = "保存或更新我的偏好")
    @PostMapping
    public Result<Preference> save(@RequestBody Preference preference) {
        // 强制绑定当前登录用户，忽略前端传来的 userId
        preference.setUserId(UserContext.currentUsername());
        return Result.success("保存成功", preferenceService.saveOrUpdate(preference));
    }

    @Operation(summary = "删除我的偏好")
    @DeleteMapping
    public Result<Void> remove() {
        Preference exist = preferenceService.getByUserId(UserContext.currentUsername());
        if (exist != null) {
            preferenceService.remove(exist.getId());
        }
        return Result.success("删除成功", null);
    }
}
