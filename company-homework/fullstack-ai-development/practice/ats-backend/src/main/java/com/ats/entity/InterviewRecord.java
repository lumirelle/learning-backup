package com.ats.entity;

import com.ats.config.PgEnumTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 面试评价记录（M4）。一个 application 可有多条（多轮面试）。
 *
 * <p><strong>编辑窗口约定</strong>：service 层限制 24h 内自己写的可改，超过即冻结。
 * 这是 review-after-the-fact 的常见折中：给面试官改错的窗口，但不允许「面试半年后追改」。</p>
 */
@Data
@TableName(value = "interview_records", autoResultMap = true)
public class InterviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    /** 面试官 user_id（允许为 null：账号被删后保留记录） */
    private Long interviewerId;

    /** 面试轮次描述，如"技术一面"、"HR 终面" */
    private String round;

    /** 1-5 星综合评分 */
    private Short rating;

    private String strengths;
    private String weaknesses;

    /** PASS / REJECT / HOLD（PG enum，需要 typeHandler） */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String conclusion;

    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
