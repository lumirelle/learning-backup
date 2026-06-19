package com.ats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("refresh_tokens")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** SHA-256(rawToken) hex，不存明文 */
    private String tokenHash;

    private OffsetDateTime expiresAt;
    private Boolean revoked;
    private OffsetDateTime createdAt;
}
