package com.ats.repository;

import com.ats.entity.SubDepartment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 子部门 Mapper。叶子节点，挂 HR、岗位、工作地点。
 */
@Mapper
public interface SubDepartmentMapper extends BaseMapper<SubDepartment> {

    /**
     * 批量按 id 查子部门（用于 jobs 详情拼装"子部门 / 工作地点 / 上层部门"展示信息）。
     * 顺带 join 上层部门拿 parent_department_name。
     */
    @Select({
            "<script>",
            "SELECT",
            "  sd.id              AS id,",
            "  sd.name            AS name,",
            "  sd.location        AS location,",
            "  sd.parent_department_id AS parent_department_id,",
            "  d.name             AS parent_department_name,",
            "  d.root_org_id      AS root_org_id,",
            "  ro.name            AS root_org_name",
            "FROM sub_departments sd",
            "JOIN departments d  ON d.id = sd.parent_department_id",
            "JOIN root_orgs ro   ON ro.id = d.root_org_id",
            "WHERE sd.id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<java.util.Map<String, Object>> selectExpandedByIds(@Param("ids") List<Long> ids);

    /**
     * 全量子部门列表，附带上层部门名 + root_org 名。
     * 前端"新建岗位"下拉、Board 子部门筛选下拉用。按 (root_org_id, parent_department_id, sd.id) 排序保证稳定输出。
     */
    @Select({
            "SELECT",
            "  sd.id              AS id,",
            "  sd.name            AS name,",
            "  sd.location        AS location,",
            "  sd.parent_department_id AS parent_department_id,",
            "  d.name             AS parent_department_name,",
            "  d.root_org_id      AS root_org_id,",
            "  ro.name            AS root_org_name",
            "FROM sub_departments sd",
            "JOIN departments d  ON d.id = sd.parent_department_id",
            "JOIN root_orgs ro   ON ro.id = d.root_org_id",
            "ORDER BY d.root_org_id ASC, sd.parent_department_id ASC, sd.id ASC"
    })
    List<java.util.Map<String, Object>> selectAllExpanded();

    @Select("SELECT COUNT(*) FROM jobs WHERE sub_department_id = #{id} AND deleted_at IS NULL")
    long countActiveJobs(@Param("id") Long id);
}
