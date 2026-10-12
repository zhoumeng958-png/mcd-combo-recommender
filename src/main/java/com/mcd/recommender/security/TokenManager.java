package com.mcd.recommender.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 会话管理器
 * <p>
 * 用 UUID 作为 Token、ConcurrentHashMap 作为存储，实现无状态登录。
 * <p>
 * 为什么不用 JWT：JWT 需要额外依赖且 Token 无法主动失效；这里用内存会话
 * 实现简单、可随时踢人下线，对比赛项目足够。若要多机部署，
 * 把 {@link #store} 换成 Redis 即可，接口不用动。
 * <p>
 * 为什么不用 HttpSession：Session 依赖 Cookie，跨域和前后端分离时较麻烦，
 * 而 Token 走请求头更通用。
 */
@Slf4j
@Component
public class TokenManager {

    /** token -> 会话信息 */
    private final Map<String, Session> store = new ConcurrentHashMap<>();

    /** Token 有效期（秒），默认 2 小时 */
    @Value("${auth.token-expire-seconds:7200}")
    private long expireSeconds;

    /**
     * 生成并保存 Token
     */
    public String createToken(LoginUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        long expireAt = System.currentTimeMillis() + expireSeconds * 1000L;
        store.put(token, new Session(user, expireAt));
        log.info("用户登录成功: username={}, token={}..., 有效期={}秒",
                user.getUsername(), token.substring(0, 8), expireSeconds);
        return token;
    }

    /**
     * 校验 Token，有效则返回登录用户，无效或过期返回 null
     */
    public LoginUser validate(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        Session session = store.get(token);
        if (session == null) {
            return null;
        }
        if (session.expireAt < System.currentTimeMillis()) {
            store.remove(token);
            return null;
        }
        return session.user;
    }

    /**
     * 退出登录，使 Token 立即失效
     */
    public void removeToken(String token) {
        if (token != null) {
            Session removed = store.remove(token);
            if (removed != null) {
                log.info("用户退出登录: username={}", removed.user.getUsername());
            }
        }
    }

    /**
     * 当前有效会话数（可用于演示或监控）
     */
    public int onlineCount() {
        return store.size();
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    /**
     * 清理所有过期会话
     */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().expireAt < now);
    }

    /** 会话信息 */
    private static class Session {
        final LoginUser user;
        final long expireAt;

        Session(LoginUser user, long expireAt) {
            this.user = user;
            this.expireAt = expireAt;
        }
    }
}
