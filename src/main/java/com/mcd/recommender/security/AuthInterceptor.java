package com.mcd.recommender.security;

import com.mcd.recommender.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器
 * <p>
 * 校验请求头中的 Token，通过则把登录用户写入 {@link UserContext}，
 * 并保证请求结束后清理 ThreadLocal。
 * <p>
 * 放行规则由 {@link com.mcd.recommender.config.WebMvcConfig} 统一配置。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /** 请求头名称 */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token 前缀 */
    private static final String TOKEN_PREFIX = "Bearer ";

    private final TokenManager tokenManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        LoginUser user = tokenManager.validate(token);

        if (user == null) {
            writeUnauthorized(response, "登录已过期或未登录，请重新登录");
            return false;
        }

        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理，否则 Tomcat 线程复用会导致下一个请求串号
        UserContext.clear();
    }

    /**
     * 从请求头提取 Token，同时兼容带/不带 Bearer 前缀两种写法
     */
    public static String resolveToken(HttpServletRequest request) {
        String raw = request.getHeader(TOKEN_HEADER);
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        raw = raw.trim();
        if (raw.regionMatches(true, 0, TOKEN_PREFIX, 0, TOKEN_PREFIX.length())) {
            return raw.substring(TOKEN_PREFIX.length()).trim();
        }
        return raw;
    }

    /**
     * 返回 401 的 JSON 响应
     * <p>
     * 拦截器在 Controller 之前执行，抛出的异常不会被 @RestControllerAdvice 捕获，
     * 因此这里手动写出统一格式的响应体。
     */
    private void writeUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = "{\"code\":" + ResultCode.UNAUTHORIZED.getCode()
                + ",\"message\":\"" + message + "\",\"data\":null,"
                + "\"timestamp\":" + System.currentTimeMillis() + "}";
        try (PrintWriter writer = response.getWriter()) {
            writer.write(body);
            writer.flush();
        } catch (Exception e) {
            log.warn("写出 401 响应失败: {}", e.getMessage());
        }
    }
}
