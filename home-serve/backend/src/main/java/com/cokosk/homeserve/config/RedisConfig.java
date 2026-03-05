package com.cokosk.homeserve.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis配置类
 */
@Configuration
public class RedisConfig {
    
    @Value("${spring.redis.host}")
    private String host;
    
    @Value("${spring.redis.port}")
    private Integer port;
    
    @Value("${spring.redis.password:}")
    private String password;
    
    @Value("${spring.redis.database}")
    private Integer database;
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String addr = "redis://" + host + ":" + port;
        config.useSingleServer()
              .setAddress(addr)
              .setPassword(password.isEmpty() ? null : password)
              .setDatabase(database)
              .setConnectionPoolSize(64)
              .setConnectionMinimumIdleSize(10);
        return Redisson.create(config);
    }
}