package com.ats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 组织树根节点（M6）。业务上固定单行：「xx科技集团」。
 * 不开放 CRUD，仅作为 departments.root_org_id 外键归属。
 */
@Data
@TableName(value = "root_orgs", autoResultMap = true)
public class RootOrg {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
