package com.ats.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * HR ↔ 子部门 多对多关联表（M6）。
 * 复合主键 (user_id, sub_department_id)，写入用 {@link com.ats.repository.HrSubDepartmentMapper#batchInsert}
 * 自定义 SQL，避免 MyBatis-Plus 主键推断歧义。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("hr_sub_departments")
public class HrSubDepartment {

    private Long userId;
    private Long subDepartmentId;
    private OffsetDateTime createdAt;
}
