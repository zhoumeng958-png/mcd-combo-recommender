package com.mcd.recommender;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 麦当劳套餐推荐助手 - 启动类
 *
 * @author MMdang
 */
@SpringBootApplication
@MapperScan("com.mcd.recommender.mapper")
public class McRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(McRecommenderApplication.class, args);
        System.out.println("\n=========================================================");
        System.out.println("  麦当劳套餐推荐助手 启动成功");
        System.out.println("  接口文档: http://localhost:8080/doc.html");
        System.out.println("  Swagger : http://localhost:8080/swagger-ui.html");
        System.out.println("=========================================================\n");
    }
}
