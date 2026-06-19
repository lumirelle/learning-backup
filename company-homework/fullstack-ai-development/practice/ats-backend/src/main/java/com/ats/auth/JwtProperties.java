package com.ats.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 映射 application.yml 中的 {@code ats.jwt.*} 段。
 *
 * <p>由 {@link com.ats.config.SecurityConfig} 通过 {@code @EnableConfigurationProperties}
 * 注册到容器，启动时校验所有 path / 整数字段。
 */
@Data
@Validated
@ConfigurationProperties(prefix = "ats.jwt")
public class JwtProperties {

    /** keypair 所在目录；dev 默认 {@code ../infra/jwt}（相对 backend cwd）；prod 通过 env 覆盖到 secret 挂载点 */
    @NotBlank
    private String keyDir;

    /** 私钥文件名（PKCS#8 PEM） */
    @NotBlank
    private String privateKeyFile;

    /** 公钥文件名（X.509 PEM） */
    @NotBlank
    private String publicKeyFile;

    /** access token 寿命（秒）。建议 ≤ 30min */
    @Positive
    private long accessTtlSeconds;

    /** refresh token 寿命（秒）。建议 7-30 day */
    @Positive
    private long refreshTtlSeconds;

    /** JWT iss claim，用于跨服务区分签发方 */
    @NotBlank
    private String issuer;

    /** Refresh token 通过 HttpOnly Cookie 下发的配置 */
    private RefreshCookie refreshCookie = new RefreshCookie();

    @Data
    public static class RefreshCookie {
        private String name = "ats_refresh";
        /** 限制 Cookie 仅 /auth/* 路径携带，减少其他请求暴露面 */
        private String path = "/api/v1/auth";
        /** Lax：跨站 POST 不带 cookie，CSRF 防御核心；跨域生产部署需改 None+Secure */
        private String sameSite = "Lax";
        /** dev:false 容忍 http；生产 HTTPS 必须 true */
        private boolean secure = false;
        /** 留空 = 当前域；多子域共享可设 .example.com */
        private String domain;
    }
}
