package com.authService.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * ================================================================
 * Redis Configuration
 * ================================================================
 * Redis do kaam karta hai auth-service mein:
 * 1. OTP storage (OtpService mein StringRedisTemplate use hota hai)
 * 2. Session cache (future use)
 *
 * RedisCacheManager: @Cacheable annotations ke liye
 * StringRedisTemplate: Direct key-value operations ke liye (OTP)
 * ================================================================
 */

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * StringRedisTemplate - OTP store/fetch ke liye
     * Auto-configured by Spring Boot if Redis is on classpath,
     * but explicitly defining for clarity.
     */

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * Cache Manager - @Cacheable annotations ke liye
     * Default TTL: 10 minutes
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

}
