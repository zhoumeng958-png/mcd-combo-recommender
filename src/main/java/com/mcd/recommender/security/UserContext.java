package com.mcd.recommender.security;

import com.mcd.recommender.common.BusinessException;
import com.mcd.recommender.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户上下文
 * <p>
 * 用 ThreadLocal 保存本次请求的登录用户，让 Service 层无需层层透传参数
 * 就能拿到「当前是谁在操作」。请求结束由拦截器调用 {@link #clear()} 释放，
 * 避免线程复用导致的用户串号。
 */
@Slf4j
@Component
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户，未登录返回 null
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录则抛 401
     */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    /**
     * 获取当前用户名（即 user_id 字段的取值）
     */
    public static String currentUsername() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    public static Long currentUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
