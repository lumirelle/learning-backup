package com.ats.repository;

import com.ats.entity.HrSubDepartment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * HR ↔ 子部门 多对多关联表 Mapper（M6）。
 * 复合主键场景，BaseMapper 仅作为查询基座；写入走自定义 SQL 避免 MyBatis-Plus 主键推断歧义。
 */
@Mapper
public interface HrSubDepartmentMapper extends BaseMapper<HrSubDepartment> {

    /**
     * 批量插入：(userId, subDepartmentId[1], subDepartmentId[2], ...) 一次写入。
     * ON CONFLICT DO NOTHING 容忍重复绑定。
     */
    @Update({
            "<script>",
            "INSERT INTO hr_sub_departments (user_id, sub_department_id)",
            "VALUES",
            "<foreach collection='subDepartmentIds' item='sid' separator=','>",
            "  (#{userId}, #{sid})",
            "</foreach>",
            "ON CONFLICT (user_id, sub_department_id) DO NOTHING",
            "</script>"
    })
    int batchInsert(@Param("userId") Long userId,
                    @Param("subDepartmentIds") List<Long> subDepartmentIds);

    @Delete("DELETE FROM hr_sub_departments WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT sub_department_id FROM hr_sub_departments WHERE user_id = #{userId} ORDER BY sub_department_id ASC")
    List<Long> selectSubDepartmentIdsByUserId(@Param("userId") Long userId);
}
