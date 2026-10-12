package com.mcd.recommender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置
 * <p>
 * 只引入 spring-security-crypto 这一个工具包，不使用完整的 Spring Security 过滤器链，
 * 因此不会影响现有的拦截器鉴权方案。
 * <p>
 * BCrypt 的特点：内置随机盐、单向不可逆、每次加密结果都不同，
 * 即使数据库泄露也无法反推出原始密码。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength 默认 10，兼顾安全性与加密速度
        return new BCryptPasswordEncoder();
    }
}
