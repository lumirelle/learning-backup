package com.ats.entity;

import com.ats.config.PgEnumTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "jobs", autoResultMap = true)
public class Job {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * M6 起岗位必须挂在子部门（叶子节点），不再持有 location / 旧 department_id。
     * 工作地点通过 join sub_departments.location 取得。
     */
    private Long subDepartmentId;
    private Long createdBy;

    private String title;
    private String description;

    /** FULL_TIME / PART_TIME / CONTRACT / INTERN / REMOTE — 映射 PG job_work_type enum */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String workType;

    /** INTERN / JUNIOR / MID / SENIOR / LEAD / DIRECTOR — 映射 PG job_level enum */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String level;

    private Integer salaryMin;
    private Integer salaryMax;
    private Short headcount;

    /** DRAFT / PUBLISHED / PAUSED / CLOSED / ARCHIVED — 映射 PG job_status enum */
    @TableField(typeHandler = PgEnumTypeHandler.class)
    private String status;

    private Integer viewCount;
    private OffsetDateTime publishedAt;
    private OffsetDateTime closedAt;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * 软删时间戳。MyBatis-Plus 把它视为逻辑删除字段：
     * 默认查询自动追加 WHERE deleted_at IS NULL；deleteById 转成 UPDATE SET deleted_at = NOW()。
     */
    @TableLogic(value = "null", delval = "NOW()")
    private OffsetDateTime deletedAt;
}
