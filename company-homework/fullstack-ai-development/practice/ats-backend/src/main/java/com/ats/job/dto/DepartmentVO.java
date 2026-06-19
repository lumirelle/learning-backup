package com.ats.job.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 部门字典 VO（中间节点）。
 * M6 起增加 rootOrgId / parentDepartmentId 字段，反映组织树位置。
 */
@Data
@Builder
public class DepartmentVO {
    private Long id;
    private String name;
    private Long rootOrgId;
    private String rootOrgName;
    /** NULL = 直挂根；非空 = 嵌套子部门 */
    private Long parentDepartmentId;
    private String parentDepartmentName;
}
