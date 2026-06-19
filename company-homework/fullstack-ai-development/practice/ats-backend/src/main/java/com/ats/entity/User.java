package com.ats.entity;

import com.ats.config.PgEnumTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "users", autoResultMap = true)
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;
    private String passwordHash;
    private String fullName;

    /** ADMIN / HR / CANDIDATE — 映射 PostgreSQL user_role enum */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String role;

    private Boolean isActive;
    /** 候选人注册时自选兴趣方向，JSON 数组字符串 */
    private String candidateInterests;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
