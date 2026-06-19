package com.ats.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组织树节点（M6）。供 Admin 部门管理页 NTree 渲染。
 * <ul>
 *   <li>ROOT — 固定根「xx科技集团」，{@code editable=false}</li>
 *   <li>DEPARTMENT — 中间节点，可嵌套</li>
 *   <li>SUB_DEPARTMENT — 叶子，含 {@code location}</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgTreeNodeVO {

    /** 前端 NTree key，格式 root-{id} / dept-{id} / sub-{id} */
    private String key;

    private String nodeType;
    private Long id;
    private String label;
    private String location;
    private Long parentDepartmentId;
    private Boolean editable;
    private List<OrgTreeNodeVO> children;
}
