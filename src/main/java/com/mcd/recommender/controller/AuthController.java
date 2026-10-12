package com.mcd.recommender.controller;

import com.mcd.recommender.common.Result;
import com.mcd.recommender.dto.LoginRequest;
import com.mcd.recommender.dto.RegisterRequest;
import com.mcd.recommender.entity.User;
import com.mcd.recommender.security.AuthInterceptor;
import com.mcd.recommender.security.LoginUser;
import com.mcd.recommender.security.TokenManager;
import com.mcd.recommender.security.UserContext;
import com.mcd.recommender.service.UserService;
import com.mcd.recommender.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证接口
 */
@Tag(name = "00 - 登录认证", description = "注册 / 登录 / 登出 / 获取当前用户")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenManager tokenManager;

    @Operation(summary = "登录", description = "默认账号 admin / 123456")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    @Operation(summary = "注册", description = "注册成功后直接返回 token，无需再登录一次")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", userService.register(request));
    }

    @Operation(summary = "退出登录", description = "使当前 token 立即失效")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        userService.logout(AuthInterceptor.resolveToken(request));
        return Result.success("已退出登录", null);
    }

    @Operation(summary = "获取当前登录用户", description = "前端刷新页面时用 token 换取用户信息")
    @GetMapping("/me")
    public Result<LoginUser> me() {
        return Result.success(UserContext.require());
    }

    @Operation(summary = "获取当前用户完整资料")
    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.success(userService.getById(UserContext.require().getUserId()));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        userService.changePassword(UserContext.require().getUserId(), oldPassword, newPassword);
        return Result.success("密码修改成功，请重新登录", null);
    }

    @Operation(summary = "更新个人资料")
    @PostMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        // 强制使用当前登录用户ID，防止越权改他人资料
        user.setId(UserContext.require().getUserId());
        return Result.success("保存成功", userService.updateProfile(user));
    }

    @Operation(summary = "在线用户数", description = "当前有效 token 数量，便于演示")
    @GetMapping("/online")
    public Result<Integer> online() {
        tokenManager.cleanExpired();
        return Result.success(tokenManager.onlineCount());
    }
}
