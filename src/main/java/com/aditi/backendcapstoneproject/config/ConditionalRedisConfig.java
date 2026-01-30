package com.aditi.backendcapstoneproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

/**
 * Conditionally configures Redis only when spring.cache.type=redis.
 *
 * This allows Redis to be enabled in production while keeping it disabled
 * (excluded) for local development with simple cache.
 * Provides RedisConnectionFactory, RedisCacheManager (so @Cacheable uses Redis),
 * and RedisTemplate.
 */
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class ConditionalRedisConfig {

    private static final List<String> CACHE_NAMES = List.of(
        "profiles", "productsById", "productsAll", "productsByCategory", "productsSearch",
        "fakestoreProductsById", "fakestoreProductsAll", "carts", "orders", "orderById", "payments"
    );

    /**
     * Explicit RedisConnectionFactory based purely on spring.redis.* properties.
     * This avoids relying on RedisAutoConfiguration, which is excluded for local dev.
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port,
            @Value("${spring.redis.password:}") String password,
            @Value("${spring.redis.ssl:false}") boolean useSsl) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
            LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(60));

        if (useSsl) {
            builder.useSsl();
        }

        LettuceClientConfiguration clientConfig = builder.build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis-backed CacheManager so that @Cacheable (e.g. productsById) uses Redis in production.
     * Uses JSON serialization so entities like Product can be stored without "Cannot serialize" errors.
     */
    @Bean
    @Primary
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${spring.cache.redis.time-to-live:600000}") long ttlMs) {
        Duration ttl = Duration.ofMillis(ttlMs);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .initialCacheNames(java.util.Set.copyOf(CACHE_NAMES))
            .build();
    }
}
