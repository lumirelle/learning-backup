package com.ats.organization;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.Department;
import com.ats.entity.RootOrg;
import com.ats.entity.SubDepartment;
import com.ats.organization.dto.DepartmentCreateReq;
import com.ats.organization.dto.DepartmentUpdateReq;
import com.ats.organization.dto.OrgTreeNodeVO;
import com.ats.organization.dto.SubDepartmentCreateReq;
import com.ats.organization.dto.SubDepartmentUpdateReq;
import com.ats.repository.DepartmentMapper;
import com.ats.repository.HrSubDepartmentMapper;
import com.ats.repository.RootOrgMapper;
import com.ats.repository.SubDepartmentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final RootOrgMapper rootOrgMapper;
    private final DepartmentMapper departmentMapper;
    private final SubDepartmentMapper subDepartmentMapper;
    private final HrSubDepartmentMapper hrSubDepartmentMapper;

    public OrgTreeNodeVO getTree() {
        RootOrg root = rootOrgMapper.selectOne(Wrappers.<RootOrg>lambdaQuery().last("LIMIT 1"));
        if (root == null) {
            throw BizException.of(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        List<Department> allDepts = departmentMapper.selectList(
                Wrappers.<Department>lambdaQuery()
                        .orderByAsc(Department::getRootOrgId)
                        .orderByAsc(Department::getParentDepartmentId)
                        .orderByAsc(Department::getId));
        List<SubDepartment> allSubs = subDepartmentMapper.selectList(
                Wrappers.<SubDepartment>lambdaQuery()
                        .orderByAsc(SubDepartment::getParentDepartmentId)
                        .orderByAsc(SubDepartment::getId));

        Map<Long, List<Department>> nestedDepts = allDepts.stream()
                .filter(d -> d.getParentDepartmentId() != null)
                .collect(Collectors.groupingBy(Department::getParentDepartmentId));
        Map<Long, List<SubDepartment>> subsByDept = allSubs.stream()
                .collect(Collectors.groupingBy(SubDepartment::getParentDepartmentId));

        List<OrgTreeNodeVO> rootChildren = allDepts.stream()
                .filter(d -> d.getParentDepartmentId() == null)
                .map(d -> buildDepartmentNode(d, nestedDepts, subsByDept))
                .toList();

        return OrgTreeNodeVO.builder()
                .key("root-" + root.getId())
                .nodeType("ROOT")
                .id(root.getId())
                .label(root.getName())
                .editable(false)
                .children(rootChildren)
                .build();
    }

    @Transactional
    public OrgTreeNodeVO createDepartment(DepartmentCreateReq req) {
        RootOrg root = requireRootOrg();
        Long parentId = req.getParentDepartmentId();
        Long rootOrgId = root.getId();

        if (parentId != null) {
            Department parent = departmentMapper.selectById(parentId);
            if (parent == null) {
                throw BizException.of(ErrorCode.DEPARTMENT_NOT_FOUND);
            }
            rootOrgId = parent.getRootOrgId();
        }

        assertDepartmentNameUnique(rootOrgId, parentId, req.getName().trim(), null);

        Department dept = new Department();
        dept.setRootOrgId(rootOrgId);
        dept.setParentDepartmentId(parentId);
        dept.setName(req.getName().trim());
        departmentMapper.insert(dept);

        log.info("[ORG] create department id={} name='{}' parent={}", dept.getId(), dept.getName(), parentId);
        return buildDepartmentNode(dept, Map.of(), Map.of());
    }

    @Transactional
    public OrgTreeNodeVO updateDepartment(Long id, DepartmentUpdateReq req) {
        Department dept = requireDepartment(id);
        assertDepartmentNameUnique(dept.getRootOrgId(), dept.getParentDepartmentId(), req.getName().trim(), id);
        dept.setName(req.getName().trim());
        departmentMapper.updateById(dept);
        log.info("[ORG] update department id={} name='{}'", id, dept.getName());
        return buildDepartmentNode(dept, Map.of(), Map.of());
    }

    @Transactional
    public void deleteDepartment(Long id) {
        requireDepartment(id);
        if (departmentMapper.countChildDepartments(id) > 0
                || departmentMapper.countSubDepartmentsUnder(id) > 0) {
            throw BizException.of(ErrorCode.DEPARTMENT_HAS_CHILDREN);
        }
        departmentMapper.deleteById(id);
        log.info("[ORG] delete department id={}", id);
    }

    @Transactional
    public OrgTreeNodeVO createSubDepartment(SubDepartmentCreateReq req) {
        requireDepartment(req.getParentDepartmentId());
        assertSubDepartmentNameUnique(req.getParentDepartmentId(), req.getName().trim(), null);

        SubDepartment sd = new SubDepartment();
        sd.setParentDepartmentId(req.getParentDepartmentId());
        sd.setName(req.getName().trim());
        sd.setLocation(req.getLocation().trim());
        subDepartmentMapper.insert(sd);

        log.info("[ORG] create sub_department id={} name='{}' location='{}'",
                sd.getId(), sd.getName(), sd.getLocation());
        return toSubNode(sd);
    }

    @Transactional
    public OrgTreeNodeVO updateSubDepartment(Long id, SubDepartmentUpdateReq req) {
        SubDepartment sd = requireSubDepartment(id);
        assertSubDepartmentNameUnique(sd.getParentDepartmentId(), req.getName().trim(), id);
        sd.setName(req.getName().trim());
        sd.setLocation(req.getLocation().trim());
        subDepartmentMapper.updateById(sd);
        log.info("[ORG] update sub_department id={}", id);
        return toSubNode(sd);
    }

    @Transactional
    public void deleteSubDepartment(Long id) {
        requireSubDepartment(id);
        if (subDepartmentMapper.countActiveJobs(id) > 0) {
            throw BizException.of(ErrorCode.SUB_DEPARTMENT_HAS_JOBS);
        }
        hrSubDepartmentMapper.delete(
                Wrappers.<com.ats.entity.HrSubDepartment>lambdaQuery()
                        .eq(com.ats.entity.HrSubDepartment::getSubDepartmentId, id));
        subDepartmentMapper.deleteById(id);
        log.info("[ORG] delete sub_department id={}", id);
    }

    private OrgTreeNodeVO buildDepartmentNode(Department d,
                                              Map<Long, List<Department>> nestedDepts,
                                              Map<Long, List<SubDepartment>> subsByDept) {
        List<OrgTreeNodeVO> children = new ArrayList<>();
        for (Department child : nestedDepts.getOrDefault(d.getId(), List.of())) {
            children.add(buildDepartmentNode(child, nestedDepts, subsByDept));
        }
        for (SubDepartment sd : subsByDept.getOrDefault(d.getId(), List.of())) {
            children.add(toSubNode(sd));
        }
        return OrgTreeNodeVO.builder()
                .key("dept-" + d.getId())
                .nodeType("DEPARTMENT")
                .id(d.getId())
                .label(d.getName())
                .parentDepartmentId(d.getParentDepartmentId())
                .editable(true)
                .children(children)
                .build();
    }

    private static OrgTreeNodeVO toSubNode(SubDepartment sd) {
        return OrgTreeNodeVO.builder()
                .key("sub-" + sd.getId())
                .nodeType("SUB_DEPARTMENT")
                .id(sd.getId())
                .label(sd.getName())
                .location(sd.getLocation())
                .parentDepartmentId(sd.getParentDepartmentId())
                .editable(true)
                .children(List.of())
                .build();
    }

    private RootOrg requireRootOrg() {
        RootOrg root = rootOrgMapper.selectOne(Wrappers.<RootOrg>lambdaQuery().last("LIMIT 1"));
        if (root == null) {
            throw BizException.of(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return root;
    }

    private Department requireDepartment(Long id) {
        Department d = departmentMapper.selectById(id);
        if (d == null) {
            throw BizException.of(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return d;
    }

    private SubDepartment requireSubDepartment(Long id) {
        SubDepartment sd = subDepartmentMapper.selectById(id);
        if (sd == null) {
            throw BizException.of(ErrorCode.SUB_DEPARTMENT_NOT_FOUND);
        }
        return sd;
    }

    private void assertDepartmentNameUnique(Long rootOrgId, Long parentDepartmentId, String name, Long excludeId) {
        LambdaQueryWrapper<Department> q = Wrappers.<Department>lambdaQuery()
                .eq(Department::getRootOrgId, rootOrgId)
                .eq(Department::getName, name);
        if (parentDepartmentId == null) {
            q.isNull(Department::getParentDepartmentId);
        } else {
            q.eq(Department::getParentDepartmentId, parentDepartmentId);
        }
        if (excludeId != null) {
            q.ne(Department::getId, excludeId);
        }
        if (departmentMapper.selectCount(q) > 0) {
            throw new BizException(ErrorCode.BIZ_RULE_VIOLATED, "同层级下已存在同名部门：" + name);
        }
    }

    private void assertSubDepartmentNameUnique(Long parentDepartmentId, String name, Long excludeId) {
        LambdaQueryWrapper<SubDepartment> q = Wrappers.<SubDepartment>lambdaQuery()
                .eq(SubDepartment::getParentDepartmentId, parentDepartmentId)
                .eq(SubDepartment::getName, name);
        if (excludeId != null) {
            q.ne(SubDepartment::getId, excludeId);
        }
        if (subDepartmentMapper.selectCount(q) > 0) {
            throw new BizException(ErrorCode.BIZ_RULE_VIOLATED, "该部门下已存在同名子部门：" + name);
        }
    }
}
