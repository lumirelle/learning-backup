package com.ats.repository;

import com.ats.entity.StageLog;
import com.ats.job.HrJobScope;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StageLogMapper extends BaseMapper<StageLog> {

    /** 按 application_id 升序时间列出全部日志，含操作人姓名（用于详情时间线）。 */
    @Select({
        "SELECT sl.id, sl.application_id, sl.from_stage, sl.to_stage, sl.note,",
        "       sl.operated_by, u.full_name AS operated_by_name, u.role AS operated_by_role,",
        "       sl.operated_at",
        "FROM stage_logs sl",
        "LEFT JOIN users u ON u.id = sl.operated_by",
        "WHERE sl.application_id = #{applicationId}",
        "ORDER BY sl.operated_at ASC, sl.id ASC"
    })
    List<Map<String, Object>> findByApplicationId(@Param("applicationId") Long applicationId);

    @org.apache.ibatis.annotations.Select({
            "<script>",
            "SELECT sl.operated_at, sl.application_id, a.job_id, j.title AS job_title,",
            "       sl.from_stage, sl.to_stage, sl.note, u.email AS operator_email",
            "FROM stage_logs sl",
            "JOIN applications a ON a.id = sl.application_id",
            "JOIN jobs j ON j.id = a.job_id",
            "LEFT JOIN users u ON u.id = sl.operated_by",
            "WHERE j.deleted_at IS NULL",
            "<if test='hrScope != null'>",
            "  AND ( j.created_by = #{hrScope.hrUserId}",
            "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
            "    OR j.sub_department_id IN",
            "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
            "  </if>",
            "  )",
            "</if>",
            "ORDER BY sl.operated_at DESC, sl.id DESC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<Map<String, Object>> exportRows(@Param("hrScope") HrJobScope hrScope,
                                           @Param("limit") int limit);
}
