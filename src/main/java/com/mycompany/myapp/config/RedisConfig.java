package com.mycompany.myapp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Cấu hình Redis Client cho CinemaTick.
 *
 * - RedissonClient: dùng cho Distributed Lock (giữ ghế tạm thời)
 * - StringRedisTemplate: dùng cho JWT Blacklist
 */
@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Tạo Redisson client độc lập (không phụ thuộc vào spring.cache.type=jcache).
     * Dùng cho DistributedLockService (giữ ghế) và JwtBlacklistService.
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Fix Hibernate lazy initialization
        config.setCodec(new org.redisson.codec.SerializationCodec());
        config
            .useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort)
            .setConnectionPoolSize(10)
            .setConnectionMinimumIdleSize(2);

        log.info("⚡ [REDIS] Khởi tạo Redisson Client → redis://{}:{}", redisHost, redisPort);
        return Redisson.create(config);
    }

    /**
     * StringRedisTemplate để thao tác key-value String đơn giản (JWT Blacklist).
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
