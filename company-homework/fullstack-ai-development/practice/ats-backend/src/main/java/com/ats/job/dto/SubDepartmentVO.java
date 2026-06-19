package com.ats.job.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 子部门 VO（叶子节点）。
 * <p>
 * 前端「新建岗位」下拉、Board 子部门筛选、HR 用户绑定下拉都用这个。
 * label 推荐展示："{name} / {location}"，详见 hr/jobs.vue 表单。
 */
@Data
@Builder
public class SubDepartmentVO {
    private Long id;
    private String name;
    private String location;
    private Long parentDepartmentId;
    private String parentDepartmentName;
    private Long rootOrgId;
    private String rootOrgName;
}
