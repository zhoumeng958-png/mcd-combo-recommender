package com.mcd.recommender.config;

import com.mcd.recommender.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册登录拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有接口
                .addPathPatterns("/api/**")
                // 以下接口无需登录即可访问
                .excludePathPatterns(
                        // 登录 / 注册（登出也需经过拦截器，先校验再注销）
                        "/api/auth/login",
                        "/api/auth/register",
                        // 快速推荐（便于浏览器直接调试，不涉及用户数据）
                        "/api/recommend/quick"
                );
    }
}
