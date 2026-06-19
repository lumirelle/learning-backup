package com.ats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 组织树中间节点（M6）。容纳子 departments 或 sub_departments。
 * <ul>
 *   <li>{@code parentDepartmentId == null}：直挂 root</li>
 *   <li>{@code parentDepartmentId != null}：嵌套子部门</li>
 * </ul>
 * 部门节点本身不关联 HR / 岗位 / 工作地点，那是 sub_departments 的职责。
 */
@Data
@TableName(value = "departments", autoResultMap = true)
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long rootOrgId;
    private Long parentDepartmentId;
    private String name;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
