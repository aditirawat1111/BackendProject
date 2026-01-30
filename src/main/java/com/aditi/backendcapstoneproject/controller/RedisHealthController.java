package com.aditi.backendcapstoneproject.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class RedisHealthController {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis")
    public String redisHealth() {
        redisTemplate.opsForValue().set("health", "ok");
        return (String) redisTemplate.opsForValue().get("health");
    }

}
