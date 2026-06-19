package com.ats.auth.dto;

import lombok.Builder;
import lombok.Data;

/** login / refresh 成功后的响应体（refresh token 走 HttpOnly Cookie，不在 body 里） */
@Data
@Builder
public class TokenVO {
    private String accessToken;
    private long expiresIn;   // access token 剩余秒数
    private String tokenType; // "Bearer"
    private MeVO user;
}
