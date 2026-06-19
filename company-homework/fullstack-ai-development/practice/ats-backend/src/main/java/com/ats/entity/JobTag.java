package com.ats.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * job_tags 关联表。复合主键 (job_id, tag_id)，无独立 id 列；
 * 实际写入用 {@link com.ats.repository.JobTagMapper#batchInsert} 自定义 SQL，避免 MyBatis-Plus 主键推断歧义。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("job_tags")
public class JobTag {

    private Long jobId;
    private Long tagId;
    private OffsetDateTime createdAt;
}
