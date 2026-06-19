package com.ats.repository;

import com.ats.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 部门（中间节点）Mapper。
 * <p>
 * 常规增删改查复用 BaseMapper；批量取部门字典（id → name）见 {@link #selectNames}。
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 批量按 id 查部门 (id, name)。VO 拼装 sub_department 的"上层部门"展示用。
     */
    @Select({
            "<script>",
            "SELECT id, name FROM departments WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<Map<String, Object>> selectNames(@Param("ids") List<Long> ids);

    @Select("SELECT COUNT(*) FROM departments WHERE parent_department_id = #{id}")
    long countChildDepartments(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sub_departments WHERE parent_department_id = #{id}")
    long countSubDepartmentsUnder(@Param("id") Long id);
}
