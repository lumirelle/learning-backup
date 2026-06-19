package com.ats.entity;

import com.ats.config.PgEnumTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "tags", autoResultMap = true)
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String slug;
    private String name;

    /** TECH / SOFT / CERT / LANG / DOMAIN — 映射 PG tag_category enum */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String category;

    private OffsetDateTime createdAt;
}
