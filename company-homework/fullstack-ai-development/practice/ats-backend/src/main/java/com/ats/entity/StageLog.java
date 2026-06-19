package com.ats.entity;

import com.ats.config.PgEnumTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 候选人阶段流转的操作审计日志，只增不删。
 * 投递时 from_stage = null + to_stage = APPLIED，备注 "候选人投递"。
 */
@Data
@TableName(value = "stage_logs", autoResultMap = true)
public class StageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;
    private Long operatedBy;

    /** 流转前阶段，NULL 代表初始状态（投递时） */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String fromStage;

    /** 流转后阶段（必填） */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String toStage;

    private String note;
    private OffsetDateTime operatedAt;
}
