package com.ats.job;

import com.ats.job.dto.DepartmentVO;
import com.ats.job.dto.SubDepartmentVO;
import com.ats.repository.DepartmentMapper;
import com.ats.repository.SubDepartmentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织字典（只读）服务。
 * <p>
 * M6 改造：原 DepartmentService 只暴露扁平部门列表；现拆为两类输出 —
 * <ul>
 *   <li>{@link #listAllDepartments()} 用于"按上层部门筛选"等粗粒度场景</li>
 *   <li>{@link #listAllSubDepartments()} 用于"创建岗位时的子部门下拉"、"看板子部门筛选" 等细粒度场景</li>
 * </ul>
 * 树形 CRUD 由 {@link com.ats.organization.OrganizationService} 承担。
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final SubDepartmentMapper subDepartmentMapper;

    /**
     * 全量部门（中间节点）列表，按 (root_org_id, parent_department_id, id) 升序。
     * 前端"按上层部门筛选"下拉用。
     */
    public List<DepartmentVO> listAllDepartments() {
        // 单次拉全量；目前预期 < 100 条，无需分页
        return departmentMapper.selectList(
                Wrappers.<com.ats.entity.Department>lambdaQuery()
                        .orderByAsc(com.ats.entity.Department::getRootOrgId,
                                    com.ats.entity.Department::getParentDepartmentId,
                                    com.ats.entity.Department::getId))
                .stream()
                .map(d -> DepartmentVO.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .rootOrgId(d.getRootOrgId())
                        .parentDepartmentId(d.getParentDepartmentId())
                        .build())
                .toList();
    }

    /**
     * 全量子部门（叶子节点）列表，附带上层部门名 + 根组织名。
     * 前端"新建岗位"子部门下拉、Board 子部门筛选下拉用。
     */
    public List<SubDepartmentVO> listAllSubDepartments() {
        return subDepartmentMapper.selectAllExpanded().stream()
                .map(DepartmentService::toSubDepartmentVO)
                .toList();
    }

    /**
     * 旧前端兼容：原 {@code GET /departments} 仍可调用 listAll()。
     * @deprecated 使用 {@link #listAllDepartments()}，命名更清晰。
     */
    @Deprecated
    public List<DepartmentVO> listAll() {
        return listAllDepartments();
    }

    private static SubDepartmentVO toSubDepartmentVO(Map<String, Object> row) {
        return SubDepartmentVO.builder()
                .id(((Number) row.get("id")).longValue())
                .name((String) row.get("name"))
                .location((String) row.get("location"))
                .parentDepartmentId(row.get("parent_department_id") == null ? null
                        : ((Number) row.get("parent_department_id")).longValue())
                .parentDepartmentName((String) row.get("parent_department_name"))
                .rootOrgId(row.get("root_org_id") == null ? null
                        : ((Number) row.get("root_org_id")).longValue())
                .rootOrgName((String) row.get("root_org_name"))
                .build();
    }

    /**
     * 这里保留 batchLoadXxx 用作 service 内部依赖收口的好习惯。
     * @return id → SubDepartmentVO 映射，便于上游按 id 取展示信息
     */
    public Map<Long, SubDepartmentVO> batchLoadSubDepartments(List<Long> subDeptIds) {
        if (subDeptIds == null || subDeptIds.isEmpty()) return Map.of();
        return subDepartmentMapper.selectExpandedByIds(subDeptIds).stream()
                .map(DepartmentService::toSubDepartmentVO)
                .collect(Collectors.toMap(SubDepartmentVO::getId, v -> v));
    }
}
