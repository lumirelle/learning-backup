package com.ats.auth;

import com.ats.auth.support.TestRsaKeyPair;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtService 纯单元测试 —— 不启 Spring。
 * keypair 在 {@link TempDir} 临时目录现场生成，跑完即删，不依赖 infra/jwt/。
 */
@DisplayName("JwtService · 签发 / 校验 / Refresh hash")
class JwtServiceTest {

    @TempDir
    static Path keyDir;

    static TestRsaKeyPair kp;
    static JwtProperties props;
    static JwtService service;

    @BeforeAll
    static void initService() throws Exception {
        kp = TestRsaKeyPair.generateAndWriteTo(keyDir, "priv.pem", "pub.pem");

        props = new JwtProperties();
        props.setKeyDir(keyDir.toString());
        props.setPrivateKeyFile("priv.pem");
        props.setPublicKeyFile("pub.pem");
        props.setAccessTtlSeconds(900);
        props.setRefreshTtlSeconds(2_592_000);
        props.setIssuer("ats.test");

        service = new JwtService(props);
        service.init();
    }

    // ── access token sign / verify ─────────────────────────────

    @Test
    @DisplayName("issue + verify happy path：claims 完整且 issuer 正确")
    void issueAndVerify_happyPath() {
        String token = service.issueAccessToken(42L, "alice@test.com", "HR");
        Claims c = service.verifyAccessToken(token);

        assertThat(c.getSubject()).isEqualTo("42");
        assertThat(c.get("email", String.class)).isEqualTo("alice@test.com");
        assertThat(c.get("role", String.class)).isEqualTo("HR");
        assertThat(c.getIssuer()).isEqualTo("ats.test");
        assertThat(c.getId()).isNotBlank();
        assertThat(c.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("verify 篡改的 token → BizException(INVALID_TOKEN)")
    void verify_tamperedSignature_throws() {
        String token = service.issueAccessToken(1L, "a@b.com", "CANDIDATE");
        // 修改最后 4 字符破坏签名
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThatThrownBy(() -> service.verifyAccessToken(tampered))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("verify 过期 token → BizException(INVALID_TOKEN)")
    void verify_expiredToken_throws() {
        Instant past = Instant.now().minusSeconds(60);
        String expired = Jwts.builder()
                .subject("1").issuer("ats.test")
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .claims(Map.of("email", "x@b.com", "role", "CANDIDATE"))
                .signWith(kp.privateKey, Jwts.SIG.RS256)
                .compact();

        assertThatThrownBy(() -> service.verifyAccessToken(expired))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("verify 错误 issuer → BizException(INVALID_TOKEN)")
    void verify_wrongIssuer_throws() {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("1").issuer("evil.com")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .claims(Map.of("email", "x@b.com", "role", "CANDIDATE"))
                .signWith(kp.privateKey, Jwts.SIG.RS256)
                .compact();

        assertThatThrownBy(() -> service.verifyAccessToken(token))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("verify 完全垃圾字符串 → BizException")
    void verify_garbage_throws() {
        assertThatThrownBy(() -> service.verifyAccessToken("not.a.jwt"))
                .isInstanceOf(BizException.class);
    }

    // ── refresh token ───────────────────────────────────────────

    @Test
    @DisplayName("generateRefreshToken 长度 ≥ 40 且每次不同")
    void refreshToken_generate_isUniqueAndLongEnough() {
        String a = service.generateRefreshToken();
        String b = service.generateRefreshToken();
        assertThat(a).isNotEqualTo(b);
        assertThat(a.length()).isGreaterThanOrEqualTo(40);
        assertThat(a).matches("[A-Za-z0-9_-]+");
    }

    @Test
    @DisplayName("hashRefreshToken 是 SHA-256 hex（64 位）且确定")
    void hashRefreshToken_isDeterministic() {
        String h1 = service.hashRefreshToken("seed-value");
        String h2 = service.hashRefreshToken("seed-value");
        String h3 = service.hashRefreshToken("seed-value2");
        assertThat(h1).isEqualTo(h2).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(h1).isNotEqualTo(h3);
    }

    @Test
    @DisplayName("TTL getter 透传 properties")
    void ttl_getters_returnPropsValues() {
        assertThat(service.getAccessTtlSeconds()).isEqualTo(900);
        assertThat(service.getRefreshTtlSeconds()).isEqualTo(2_592_000);
    }
}
