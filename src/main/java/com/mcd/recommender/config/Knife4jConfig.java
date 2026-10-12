package com.mcd.recommender.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI3 接口文档配置
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("麦当劳套餐推荐助手 API")
                        .description("基于规则策略引擎的麦当劳套餐智能推荐服务。\n\n"
                                + "核心接口：POST /api/recommend 传入预算、口味、人数等偏好，返回带推荐理由的套餐榜单。")
                        .version("1.0.0")
                        .contact(new Contact().name("MMdang").email("mmdang@example.com"))
                        .license(new License().name("Apache 2.0")));
    }
}
