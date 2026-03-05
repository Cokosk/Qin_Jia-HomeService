package com.cokosk.homeserve;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 家政服务平台启动类
 */
@SpringBootApplication
@MapperScan("com.cokosk.homeserve.mapper")
public class HomeServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeServeApplication.class, args);
    }
}