package com.ats.controller;

import com.ats.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;

    @Value("${spring.application.name:ats-backend}")
    private String appName;

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("app", appName);
        body.put("time", Instant.now().toString());
        body.put("db", probe(() -> jdbc.queryForObject("SELECT 1", Integer.class) != null));
        body.put("redis", probe(() -> "PONG".equalsIgnoreCase(redis.getConnectionFactory().getConnection().ping())));
        return ApiResponse.ok(body);
    }

    private static String probe(SafeCheck c) {
        try {
            return c.run() ? "UP" : "DOWN";
        } catch (Exception ex) {
            log.warn("health probe failed", ex);
            return "DOWN";
        }
    }

    @FunctionalInterface
    private interface SafeCheck {
        boolean run() throws Exception;
    }
}
