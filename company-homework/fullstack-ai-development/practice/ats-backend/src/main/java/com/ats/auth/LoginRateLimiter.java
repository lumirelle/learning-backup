package com.ats.auth;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录失败限流：同一邮箱 15 分钟内最多 10 次失败尝试。
 */
@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    public void checkAllowed(String email) {
        String key = "login:fail:" + email.toLowerCase();
        String val = redis.opsForValue().get(key);
        if (val != null && Integer.parseInt(val) >= MAX_ATTEMPTS) {
            throw new BizException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS,
                    "登录尝试过多，请 15 分钟后再试");
        }
    }

    public void recordFailure(String email) {
        String key = "login:fail:" + email.toLowerCase();
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, WINDOW);
        }
    }

    public void clearFailures(String email) {
        redis.delete("login:fail:" + email.toLowerCase());
    }
}
