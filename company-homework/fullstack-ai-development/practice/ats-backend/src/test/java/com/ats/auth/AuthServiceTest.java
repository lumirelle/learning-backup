package com.ats.auth;

import com.ats.auth.dto.LoginReq;
import com.ats.auth.dto.MeVO;
import com.ats.auth.dto.RegisterReq;
import com.ats.auth.dto.TokenVO;
import com.ats.auth.util.CookieUtil;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.RefreshToken;
import com.ats.entity.User;
import org.mockito.ArgumentMatcher;
import com.ats.repository.RefreshTokenMapper;
import com.ats.repository.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AuthService 纯 Mockito 单测，覆盖所有分支：
 *  · register：duplicate / happy
 *  · login   ：wrong-pwd / unknown-email / disabled / happy（带 token rotation 验证）
 *  · refresh ：missing-cookie / unknown-hash / revoked / expired / user-gone / happy
 *  · logout  ：with-cookie / no-cookie
 *  · me      ：missing-user
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService · 业务分支全覆盖")
class AuthServiceTest {

    @Mock UserMapper userMapper;
    @Mock RefreshTokenMapper refreshTokenMapper;
    @Mock JwtService jwtService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CookieUtil cookieUtil;
    @Mock LoginRateLimiter loginRateLimiter;

    @InjectMocks AuthService authService;

    HttpServletRequest req;
    HttpServletResponse resp;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "objectMapper", new ObjectMapper());
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
    }

    // ───────────────────── register ─────────────────────

    @Nested
    @DisplayName("register · 候选人自助注册")
    class Register {

        @Test
        @DisplayName("happy path：邮箱小写化 + role 强制 CANDIDATE + isActive=true")
        void happyPath() {
            when(userMapper.selectCount(any())).thenReturn(0L);
            when(passwordEncoder.encode("PassW0rd!")).thenReturn("BCRYPT-HASH");

            RegisterReq req = new RegisterReq();
            req.setEmail("Alice@Test.COM");
            req.setPassword("PassW0rd!");
            req.setFullName("Alice");

            MeVO me = authService.register(req);

            assertThat(me.getEmail()).isEqualTo("alice@test.com");
            assertThat(me.getRole()).isEqualTo("CANDIDATE");
            assertThat(me.getFullName()).isEqualTo("Alice");
            verify(userMapper).insert(argThatUser(u ->
                    "alice@test.com".equals(u.getEmail())
                            && "BCRYPT-HASH".equals(u.getPasswordHash())
                            && "CANDIDATE".equals(u.getRole())
                            && Boolean.TRUE.equals(u.getIsActive())));
        }

        @Test
        @DisplayName("邮箱已存在 → EMAIL_ALREADY_EXISTS 且不 insert")
        void emailDuplicate_throws() {
            when(userMapper.selectCount(any())).thenReturn(1L);

            RegisterReq req = new RegisterReq();
            req.setEmail("dup@test.com");
            req.setPassword("p");
            req.setFullName("D");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BizException.class)
                    .extracting(e -> ((BizException) e).getErrorCode())
                    .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

            verify(userMapper, never()).insert(any(User.class));
        }
    }

    // ───────────────────── login ─────────────────────

    @Nested
    @DisplayName("login · 密码 + 状态校验 + token rotation")
    class Login {

        @Test
        @DisplayName("happy path：签发 access + 写 refresh + 旧 refresh 全 revoke")
        void happyPath_revokesOldAndIssuesNew() {
            User u = newUser(7L, "hr@test.com", "HR", true);
            when(userMapper.selectOne(any())).thenReturn(u);
            when(passwordEncoder.matches("pwd", "BCRYPT")).thenReturn(true);
            when(jwtService.issueAccessToken(7L, "hr@test.com", "HR")).thenReturn("ACCESS");
            when(jwtService.generateRefreshToken()).thenReturn("RAW-REFRESH");
            when(jwtService.hashRefreshToken("RAW-REFRESH")).thenReturn("HASH");
            when(jwtService.getAccessTtlSeconds()).thenReturn(900L);
            when(jwtService.getRefreshTtlSeconds()).thenReturn(86_400L);

            LoginReq lr = loginReq("hr@test.com", "pwd");
            TokenVO vo = authService.login(lr, resp);

            assertThat(vo.getAccessToken()).isEqualTo("ACCESS");
            assertThat(vo.getExpiresIn()).isEqualTo(900);
            assertThat(vo.getTokenType()).isEqualTo("Bearer");
            assertThat(vo.getUser().getRole()).isEqualTo("HR");

            verify(refreshTokenMapper).revokeAllByUserId(7L);
            verify(refreshTokenMapper).insert(argThatRefresh(rt ->
                    "HASH".equals(rt.getTokenHash())
                            && rt.getUserId() == 7L
                            && !rt.getRevoked()));
            verify(cookieUtil).writeRefreshCookie(resp, "RAW-REFRESH", 86_400L);
        }

        @Test
        @DisplayName("密码错误 → INVALID_CREDENTIALS 且不签发任何 token")
        void wrongPassword_throws() {
            User u = newUser(1L, "a@b.com", "CANDIDATE", true);
            when(userMapper.selectOne(any())).thenReturn(u);
            when(passwordEncoder.matches(any(), any())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginReq("a@b.com", "wrong"), resp))
                    .isInstanceOf(BizException.class)
                    .extracting(e -> ((BizException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

            verify(refreshTokenMapper, never()).insert(any(RefreshToken.class));
            verifyNoInteractions(cookieUtil);
        }

        @Test
        @DisplayName("邮箱不存在 → INVALID_CREDENTIALS（不暴露邮箱是否存在）")
        void unknownEmail_throwsSameError() {
            when(userMapper.selectOne(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(loginReq("x@b.com", "p"), resp))
                    .extracting(e -> ((BizException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("账号已禁用（isActive=false）→ USER_DISABLED")
        void disabledUser_throws() {
            User u = newUser(1L, "a@b.com", "HR", false);
            when(userMapper.selectOne(any())).thenReturn(u);
            when(passwordEncoder.matches(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> authService.login(loginReq("a@b.com", "p"), resp))
                    .extracting(e -> ((BizException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_DISABLED);
        }
    }

    // ───────────────────── refresh ─────────────────────

    @Nested
    @DisplayName("refresh · rotation + 各种失效场景清 cookie")
    class Refresh {

        @Test
        @DisplayName("cookie 缺失 → REFRESH_TOKEN_INVALID")
        void missingCookie_throws() {
            when(cookieUtil.readRefreshCookie(req)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh(req, resp))
                    .extracting(e -> ((BizException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        @Test
        @DisplayName("hash 在 DB 不存在 → 抛错并清 cookie")
        void unknownHash_clearsCookie() {
            when(cookieUtil.readRefreshCookie(req)).thenReturn(Optional.of("RAW"));
            when(jwtService.hashRefreshToken("RAW")).thenReturn("HASH");
            when(refreshTokenMapper.selectOne(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.refresh(req, resp));
            verify(cookieUtil).clearRefreshCookie(resp);
        }

        @Test
        @DisplayName("已 revoked → 抛错并清 cookie（防 replay 攻击）")
        void revokedToken_clearsCookie() {
            stubRefreshLookup("RAW", "HASH", refreshTokenRecord(true, OffsetDateTime.now().plusDays(7), 1L));

            assertThatThrownBy(() -> authService.refresh(req, resp));
            verify(cookieUtil).clearRefreshCookie(resp);
            verify(refreshTokenMapper, never()).updateById(any(RefreshToken.class));
            verify(refreshTokenMapper, never()).insert(any(RefreshToken.class));
        }

        @Test
        @DisplayName("已过期 → 抛错并清 cookie")
        void expiredToken_clearsCookie() {
            stubRefreshLookup("RAW", "HASH", refreshTokenRecord(false, OffsetDateTime.now().minusSeconds(1), 1L));

            assertThatThrownBy(() -> authService.refresh(req, resp));
            verify(cookieUtil).clearRefreshCookie(resp);
        }

        @Test
        @DisplayName("用户已被删 / 禁用 → 抛错并清 cookie")
        void userGoneOrDisabled_clearsCookie() {
            stubRefreshLookup("RAW", "HASH", refreshTokenRecord(false, OffsetDateTime.now().plusDays(1), 99L));
            when(userMapper.selectById(99L)).thenReturn(null);

            assertThatThrownBy(() -> authService.refresh(req, resp));
            verify(cookieUtil).clearRefreshCookie(resp);
            verify(refreshTokenMapper).updateById(any(RefreshToken.class)); // 即便用户不存在，旧 token 仍被 revoke
            verify(refreshTokenMapper, never()).insert(any(RefreshToken.class));
        }

        @Test
        @DisplayName("happy path：旧 token 立即 revoke + 新 token 入库 + 新 cookie 下发")
        void happyPath_rotatesToken() {
            stubRefreshLookup("OLD", "OLD_HASH", refreshTokenRecord(false, OffsetDateTime.now().plusDays(7), 7L));
            User u = newUser(7L, "hr@b.com", "HR", true);
            when(userMapper.selectById(7L)).thenReturn(u);
            when(jwtService.issueAccessToken(7L, "hr@b.com", "HR")).thenReturn("NEW_ACCESS");
            when(jwtService.generateRefreshToken()).thenReturn("NEW_RAW");
            when(jwtService.hashRefreshToken("NEW_RAW")).thenReturn("NEW_HASH");
            when(jwtService.getAccessTtlSeconds()).thenReturn(900L);
            when(jwtService.getRefreshTtlSeconds()).thenReturn(86_400L);

            TokenVO vo = authService.refresh(req, resp);

            assertThat(vo.getAccessToken()).isEqualTo("NEW_ACCESS");

            verify(refreshTokenMapper).updateById(argThatRefresh(RefreshToken::getRevoked));
            verify(refreshTokenMapper).insert(argThatRefresh(r -> "NEW_HASH".equals(r.getTokenHash())));
            verify(cookieUtil).writeRefreshCookie(resp, "NEW_RAW", 86_400L);
        }
    }

    // ───────────────────── logout ─────────────────────

    @Nested
    @DisplayName("logout · 幂等 + 清 cookie")
    class Logout {

        @Test
        @DisplayName("无 cookie 也清 cookie 头（前端 401 后 fallback logout 仍能成功）")
        void noCookie_clearsCookieOnly() {
            when(cookieUtil.readRefreshCookie(req)).thenReturn(Optional.empty());

            authService.logout(req, resp);

            verify(cookieUtil).clearRefreshCookie(resp);
            verifyNoInteractions(refreshTokenMapper);
        }

        @Test
        @DisplayName("有效 cookie → 吊销对应 refresh + 清 cookie")
        void withValidCookie_revokesAndClears() {
            stubRefreshLookup("R", "H", refreshTokenRecord(false, OffsetDateTime.now().plusDays(7), 1L));

            authService.logout(req, resp);

            verify(refreshTokenMapper).updateById(argThatRefresh(RefreshToken::getRevoked));
            verify(cookieUtil).clearRefreshCookie(resp);
        }

        @Test
        @DisplayName("cookie 对应的 token 已被 revoke → 跳过 update（幂等）")
        void alreadyRevoked_skipsUpdate() {
            stubRefreshLookup("R", "H", refreshTokenRecord(true, OffsetDateTime.now().plusDays(7), 1L));

            authService.logout(req, resp);

            verify(refreshTokenMapper, never()).updateById(any(RefreshToken.class));
            verify(cookieUtil).clearRefreshCookie(resp);
        }
    }

    // ───────────────────── me ─────────────────────

    @Test
    @DisplayName("me · 用户不存在 → UNAUTHORIZED（用 stale token 调用的情况）")
    void me_userNotFound_throwsUnauthorized() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> authService.me(99L))
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("me · happy path 返回 MeVO")
    void me_happyPath() {
        when(userMapper.selectById(7L)).thenReturn(newUser(7L, "x@b.com", "HR", true));

        MeVO me = authService.me(7L);
        assertThat(me.getId()).isEqualTo(7L);
        assertThat(me.getRole()).isEqualTo("HR");
    }

    // ───────────────────── helpers ─────────────────────

    private void stubRefreshLookup(String raw, String hash, RefreshToken stored) {
        when(cookieUtil.readRefreshCookie(req)).thenReturn(Optional.of(raw));
        when(jwtService.hashRefreshToken(raw)).thenReturn(hash);
        when(refreshTokenMapper.selectOne(any())).thenReturn(stored);
    }

    private static User newUser(long id, String email, String role, boolean active) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPasswordHash("BCRYPT");
        u.setFullName(email.split("@")[0]);
        u.setRole(role);
        u.setIsActive(active);
        return u;
    }

    private static RefreshToken refreshTokenRecord(boolean revoked, OffsetDateTime expiresAt, long userId) {
        RefreshToken rt = new RefreshToken();
        rt.setId(1L);
        rt.setUserId(userId);
        rt.setTokenHash("HASH");
        rt.setRevoked(revoked);
        rt.setExpiresAt(expiresAt);
        return rt;
    }

    private static LoginReq loginReq(String email, String pwd) {
        LoginReq r = new LoginReq();
        r.setEmail(email);
        r.setPassword(pwd);
        return r;
    }

    /**
     * 类型化 ArgumentMatcher — MyBatis-Plus 3.5.9 的 BaseMapper 对 insert/updateById
     * 同时声明了 (T) 和 (Collection<T>) 重载，{@code argThat(u -> ...)} lambda 无法
     * 推断类型导致编译歧义；这里固定为 User / RefreshToken 单参版本。
     */
    private static User argThatUser(ArgumentMatcher<User> m) {
        return argThat(m);
    }

    private static RefreshToken argThatRefresh(ArgumentMatcher<RefreshToken> m) {
        return argThat(m);
    }
}
