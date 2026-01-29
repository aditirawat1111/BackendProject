package com.aditi.backendcapstoneproject.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;

/**
 * Cache configuration to make Redis failures non-fatal.
 *
 * If Redis is temporarily unavailable (for example, during Azure Cache for Redis
 * networking hiccups or misconfiguration), cache operations will log a warning
 * and fall back to hitting the database instead of failing the entire request
 * with HTTP 500.
 */
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("GET", exception, cache, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logCacheError("PUT", exception, cache, key);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("EVICT", exception, cache, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logCacheError("CLEAR", exception, cache, null);
            }

            private void logCacheError(String operation, RuntimeException exception, Cache cache, Object key) {
                String cacheName = cache != null ? cache.getName() : "unknown";

                if (exception instanceof RedisConnectionFailureException) {
                    logger.warn(
                            "Redis cache {} error during {} for key {}. Falling back to direct DB access. Cause: {}",
                            cacheName,
                            operation,
                            key,
                            exception.getMessage()
                    );
                } else {
                    logger.warn(
                            "Cache {} error during {} for key {}. Falling back to direct DB access. Cause: {}",
                            cacheName,
                            operation,
                            key,
                            exception.getMessage()
                    );
                }

                // Do NOT rethrow the exception -> let the underlying method execute
            }
        };
    }
}

