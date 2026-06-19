package com.ats.auth;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * 鉴权核心：负责 RSA keypair 加载、access token 签发/校验、refresh token 生成/哈希。
 *
 * <p>Access token = JWT (RS256, iss/sub/exp/jti + email/role 自定义 claim)。
 * Refresh token = 32 随机字节的 Base64URL 字符串（**非 JWT**），客户端只持有原值，
 * 服务端只存 SHA-256(hex)；这样即便 DB 泄露也不可被直接用作 refresh。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final JwtProperties props;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    void init() {
        try {
            Path keyDir = Path.of(props.getKeyDir()).toAbsolutePath().normalize();
            log.info("[JWT] loading RSA keypair from {} (private={} public={})",
                    keyDir, props.getPrivateKeyFile(), props.getPublicKeyFile());

            byte[] privBytes = decodePem(Files.readString(
                    keyDir.resolve(props.getPrivateKeyFile()), StandardCharsets.UTF_8));
            byte[] pubBytes = decodePem(Files.readString(
                    keyDir.resolve(props.getPublicKeyFile()), StandardCharsets.UTF_8));

            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
            this.publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(pubBytes));

            log.info("[JWT] RSA keypair loaded · access={}s refresh={}s issuer={}",
                    props.getAccessTtlSeconds(), props.getRefreshTtlSeconds(), props.getIssuer());
        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "JWT 启动初始化失败：无法读取 RSA 密钥对（keyDir=" + props.getKeyDir() + "）", e);
        }
    }

    /**
     * 签发 access token。payload 含 sub(userId) / email / role / iss / exp / jti。
     */
    public String issueAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuer(props.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(props.getAccessTtlSeconds())))
                .claims(Map.of("email", email, "role", role))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * 校验 + 解析 access token。失败统一抛 {@code BizException(INVALID_TOKEN)}。
     */
    public Claims verifyAccessToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(props.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        catch (JwtException e) {
            throw new BizException(ErrorCode.INVALID_TOKEN, "Token 无效或已过期");
        }
    }

    /**
     * 生成 refresh token（opaque random，非 JWT）；客户端持有原值，服务端只存 hash。
     * Base64URL without padding，安全用于 cookie value。
     */
    public String generateRefreshToken() {
        byte[] buf = new byte[REFRESH_TOKEN_BYTES];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** SHA-256(raw) hex，存 {@code refresh_tokens.token_hash} 列 */
    public String hashRefreshToken(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用，JVM 实现异常", e);
        }
    }

    public long getAccessTtlSeconds() {
        return props.getAccessTtlSeconds();
    }

    public long getRefreshTtlSeconds() {
        return props.getRefreshTtlSeconds();
    }

    /** 去掉 PEM 头尾 + 所有空白，base64 decode。兼容 BEGIN PRIVATE KEY / BEGIN PUBLIC KEY 等 */
    private static byte[] decodePem(String pem) {
        String b64 = pem.replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(b64);
    }
}
