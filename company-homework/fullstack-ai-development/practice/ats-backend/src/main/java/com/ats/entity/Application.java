package com.ats.entity;

import com.ats.config.PgEnumTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "applications", autoResultMap = true)
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;
    private Long candidateId;

    /** APPLIED / SCREENING_PASS / PHONE_INTERVIEW / TECH_INTERVIEW / HR_INTERVIEW / OFFER / HIRED / REJECTED */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String stage;

    private String resumeUrl;
    private Short yearsExp;
    private String phone;

    /** 拒绝原因，仅 REJECTED 时填写。 */
    private String rejectReason;

    private OffsetDateTime appliedAt;
    private OffsetDateTime updatedAt;
}
