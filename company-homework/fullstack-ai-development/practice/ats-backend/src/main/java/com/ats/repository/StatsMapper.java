package com.ats.repository;

import com.ats.job.HrJobScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.OffsetDateTime;

/**
 * 数据看板专用聚合查询（M5）。不绑 entity，纯标量返回。
 *
 * <h3>切片约定</h3>
 * <ul>
 *   <li><code>hrScope == null</code> → ADMIN 视角，不裁剪</li>
 *   <li><code>hrScope != null</code> → HR 视角，本人岗位 + 绑定子部门岗位</li>
 * </ul>
 *
 * <h3>性能</h3>
 * 所有查询都走 application/jobs/stage_logs 现有索引，O(log n)。当数据量进入 10w+ 量级
 * 时可考虑物化视图（M5 暂不优化）。
 */
@Mapper
public interface StatsMapper {

    /** 本月新增投递数。 */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM applications a",
            "JOIN jobs j ON j.id = a.job_id",
            "WHERE j.deleted_at IS NULL",
            "  AND a.applied_at >= #{since}",
            "<if test='hrScope != null'>",
            "  AND ( j.created_by = #{hrScope.hrUserId}",
            "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
            "    OR j.sub_department_id IN",
            "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
            "  </if>",
            "  )",
            "</if>",
            "</script>"
    })
    long countNewApplications(@Param("since") OffsetDateTime since,
                              @Param("hrScope") HrJobScope hrScope);

    /**
     * 本月推进到指定 stage 的<strong>次数</strong>（基于 stage_logs，更接近"本月动作"语义；
     * 用 applications.stage 当前快照统计会漏掉再次回退/二次推进，但本系统状态机不可回退，
     * 实际等价。仍走 stage_logs 留作未来可回滚状态机时的语义稳定性）。
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM stage_logs sl",
            "JOIN applications a ON a.id = sl.application_id",
            "JOIN jobs j ON j.id = a.job_id",
            "WHERE j.deleted_at IS NULL",
            "  AND sl.to_stage = #{toStage}::application_stage",
            "  AND sl.operated_at >= #{since}",
            "<if test='hrScope != null'>",
            "  AND ( j.created_by = #{hrScope.hrUserId}",
            "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
            "    OR j.sub_department_id IN",
            "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
            "  </if>",
            "  )",
            "</if>",
            "</script>"
    })
    long countTransitionsToStage(@Param("since") OffsetDateTime since,
                                 @Param("toStage") String toStage,
                                 @Param("hrScope") HrJobScope hrScope);

    /** 当前 status = PUBLISHED 的岗位数。 */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM jobs j",
            "WHERE j.deleted_at IS NULL",
            "  AND j.status = 'PUBLISHED'::job_status",
            "<if test='hrScope != null'>",
            "  AND ( j.created_by = #{hrScope.hrUserId}",
            "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
            "    OR j.sub_department_id IN",
            "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
            "  </if>",
            "  )",
            "</if>",
            "</script>"
    })
    long countActiveJobs(@Param("hrScope") HrJobScope hrScope);

    /**
     * 已被在招岗位覆盖的部门数 —— "至少有 1 个 PUBLISHED 岗位的子部门，所归属的上层部门"。
     * <p>
     * M6：jobs 不再直接挂部门 id，需穿透 sub_departments → departments.parent_department_id？
     * 实际"上层部门"指的是 sub_departments.parent_department_id 这一层（中间部门）。
     * 比单纯 count(*) FROM departments 更有业务意义：避免出现"5 个部门 0 个岗位"的尴尬展示。
     */
    @Select("""
            SELECT COUNT(DISTINCT sd.parent_department_id)
            FROM jobs j
            JOIN sub_departments sd ON sd.id = j.sub_department_id
            WHERE j.deleted_at IS NULL
              AND j.status = 'PUBLISHED'::job_status
            """)
    long countCoveredDepartments();
}
