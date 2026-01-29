package com.aditi.backendcapstoneproject.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Conditionally configures Redis only when spring.cache.type=redis.
 * 
 * This allows Redis to be enabled in production while keeping it disabled
 * (excluded) for local development with simple cache.
 */
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class ConditionalRedisConfig {

    @Bean
    @Primary
    @SuppressWarnings("null")
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        String host = redisProperties.getHost();
        if (host != null) {
            config.setHostName(host);
        } else {
            config.setHostName("localhost"); // Default fallback
        }
        config.setPort(redisProperties.getPort());
        CharSequence password = redisProperties.getPassword();
        if (password != null && password.length() > 0) {
            config.setPassword(password.toString());
        }
        
        // Configure Lettuce client with SSL support if enabled
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = 
            LettuceClientConfiguration.builder();
        
        // Set timeout if configured, default to 60 seconds
        Duration timeout = redisProperties.getTimeout();
        Duration commandTimeout = timeout != null ? timeout : Duration.ofSeconds(60);
        builder.commandTimeout(commandTimeout);
        
        // Configure SSL if enabled
        if (redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled()) {
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
}
