package com.ats.auth;

import com.ats.auth.dto.ChangePasswordReq;
import com.ats.auth.dto.LoginReq;
import com.ats.auth.dto.MeVO;
import com.ats.auth.dto.ProfileUpdateReq;
import com.ats.auth.dto.RegisterReq;
import com.ats.auth.dto.TokenVO;
import com.ats.common.security.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ats.auth.util.CookieUtil;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.RefreshToken;
import com.ats.entity.User;
import com.ats.repository.RefreshTokenMapper;
import com.ats.repository.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;
    private final LoginRateLimiter loginRateLimiter;
    private final ObjectMapper objectMapper;

    /** 候选人自助注册（role 强制为 CANDIDATE）*/
    @Transactional
    public MeVO register(RegisterReq req) {
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail().toLowerCase()));
        if (count > 0) throw BizException.of(ErrorCode.EMAIL_ALREADY_EXISTS);

        User user = new User();
        user.setEmail(req.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setRole("CANDIDATE");
        user.setIsActive(true);
        user.setCandidateInterests(serializeInterests(req.getInterests()));
        userMapper.insert(user);

        log.info("[AUTH] register candidate id={} email={}", user.getId(), user.getEmail());
        return toMeVO(user);
    }

    /** 登录：验证密码 → 签发 access token + 生成 refresh token + 写 cookie */
    @Transactional
    public TokenVO login(LoginReq req, HttpServletResponse response) {
        String email = req.getEmail().toLowerCase();
        loginRateLimiter.checkAllowed(email);

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));

        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            loginRateLimiter.recordFailure(email);
            throw BizException.of(ErrorCode.INVALID_CREDENTIALS);
        }
        loginRateLimiter.clearFailures(email);
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw BizException.of(ErrorCode.USER_DISABLED);
        }

        // 吊销该用户已有的 refresh token（single-session 策略；多端可删此行）
        refreshTokenMapper.revokeAllByUserId(user.getId());

        return issueTokens(user, response);
    }

    /**
     * 刷新 access token（rotation 策略：旧 token 失效 + 生成新 token）。
     * cookie 由 {@link CookieUtil#readRefreshCookie} 读取，无需前端显式传参。
     */
    @Transactional
    public TokenVO refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = cookieUtil.readRefreshCookie(request)
                .orElseThrow(() -> BizException.of(ErrorCode.REFRESH_TOKEN_INVALID));

        String hash = jwtService.hashRefreshToken(rawToken);
        RefreshToken stored = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getTokenHash, hash));

        if (stored == null
                || Boolean.TRUE.equals(stored.getRevoked())
                || stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            cookieUtil.clearRefreshCookie(response);
            throw BizException.of(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // rotation：立即吊销旧 token
        stored.setRevoked(true);
        refreshTokenMapper.updateById(stored);

        User user = userMapper.selectById(stored.getUserId());
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            cookieUtil.clearRefreshCookie(response);
            throw BizException.of(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        return issueTokens(user, response);
    }

    /** 登出：吊销 cookie 中的 refresh token + 清除 cookie */
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieUtil.readRefreshCookie(request).ifPresent(raw -> {
            String hash = jwtService.hashRefreshToken(raw);
            RefreshToken stored = refreshTokenMapper.selectOne(
                    new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getTokenHash, hash));
            if (stored != null && !Boolean.TRUE.equals(stored.getRevoked())) {
                stored.setRevoked(true);
                refreshTokenMapper.updateById(stored);
            }
        });
        cookieUtil.clearRefreshCookie(response);
    }

    /** 当前登录用户信息 */
    public MeVO me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        return toMeVO(user);
    }

    @Transactional
    public MeVO updateProfile(Long userId, ProfileUpdateReq req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getInterests() != null) {
            if (!SecurityUtil.isCandidate()) {
                throw new BizException(ErrorCode.FORBIDDEN, "仅候选人可更新兴趣标签");
            }
            user.setCandidateInterests(serializeInterests(req.getInterests()));
        }
        userMapper.updateById(user);
        return toMeVO(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordReq req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.INVALID_CREDENTIALS, "当前密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
        refreshTokenMapper.revokeAllByUserId(userId);
        log.info("[AUTH] password changed userId={}", userId);
    }

    // ── 私有辅助 ──────────────────────────────────────────

    private TokenVO issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getEmail(), user.getRole());

        String rawRefresh = jwtService.generateRefreshToken();
        String hash = jwtService.hashRefreshToken(rawRefresh);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(hash);
        rt.setExpiresAt(OffsetDateTime.now().plusSeconds(jwtService.getRefreshTtlSeconds()));
        rt.setRevoked(false);
        refreshTokenMapper.insert(rt);

        cookieUtil.writeRefreshCookie(response, rawRefresh, jwtService.getRefreshTtlSeconds());

        return TokenVO.builder()
                .accessToken(accessToken)
                .expiresIn(jwtService.getAccessTtlSeconds())
                .tokenType("Bearer")
                .user(toMeVO(user))
                .build();
    }

    private MeVO toMeVO(User user) {
        return MeVO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .interests(parseInterests(user.getCandidateInterests()))
                .build();
    }

    private String serializeInterests(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(interests);
        }
        catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "兴趣标签格式无效");
        }
    }

    private List<String> parseInterests(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        }
        catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
