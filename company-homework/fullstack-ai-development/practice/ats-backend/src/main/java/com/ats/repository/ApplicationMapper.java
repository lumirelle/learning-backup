package com.ats.repository;

import com.ats.entity.Application;
import com.ats.job.HrJobScope;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    /**
     * 看板视图：按 stage 聚合该岗位（或当前 HR 名下全部岗位）的投递数。
     * 用 {@code GROUP BY stage} 一次查询返回 8 行（包含计数为 0 的阶段需在 service 里补齐）。
     *
     * @param jobId 单岗位看板用；为 null 则按 HR ownerId 名下所有岗位聚合
     * @param hrScope HR 视角：本人岗位 + 绑定子部门岗位；Admin 传 null
     */
    @Select({
        "<script>",
        "SELECT a.stage AS stage, COUNT(*) AS cnt",
        "FROM applications a",
        "JOIN jobs j ON j.id = a.job_id",
        "WHERE j.deleted_at IS NULL",
        "<if test='jobId != null'> AND a.job_id = #{jobId} </if>",
        "<if test='hrScope != null'>",
        "  AND ( j.created_by = #{hrScope.hrUserId}",
        "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
        "    OR j.sub_department_id IN",
        "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
        "  </if>",
        "  )",
        "</if>",
        "GROUP BY a.stage",
        "</script>"
    })
    List<Map<String, Object>> countByStage(@Param("jobId") Long jobId,
                                           @Param("hrScope") HrJobScope hrScope);

    /**
     * 看板每列的投递明细（含候选人姓名 + 邮箱 + 投递时间 + 最近变更时间）。
     * 同时返回 job_id / job_title 供前端跨岗位看板分组。
     */
    @Select({
        "<script>",
        "SELECT a.id, a.job_id, j.title AS job_title, a.candidate_id,",
        "       u.full_name AS candidate_name, u.email AS candidate_email,",
        "       a.stage, a.applied_at, a.updated_at, a.years_exp",
        "FROM applications a",
        "JOIN jobs j  ON j.id = a.job_id",
        "JOIN users u ON u.id = a.candidate_id",
        "WHERE j.deleted_at IS NULL",
        "<if test='jobId != null'> AND a.job_id = #{jobId} </if>",
        "<if test='hrScope != null'>",
        "  AND ( j.created_by = #{hrScope.hrUserId}",
        "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
        "    OR j.sub_department_id IN",
        "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
        "  </if>",
        "  )",
        "</if>",
        "<if test='stage != null'> AND a.stage = #{stage}::application_stage </if>",
        "ORDER BY a.updated_at DESC",
        "<if test='offset != null'> OFFSET #{offset} </if>",
        "<if test='limit != null'> LIMIT #{limit} </if>",
        "</script>"
    })
    List<Map<String, Object>> listBoardItems(@Param("jobId") Long jobId,
                                             @Param("hrScope") HrJobScope hrScope,
                                             @Param("stage") String stage,
                                             @Param("offset") Integer offset,
                                             @Param("limit") Integer limit);

    /** 候选人「我的投递」：按 candidate_id 列出，带岗位标题与最近 stage。 */
    @Select({
        "SELECT a.id, a.job_id, j.title AS job_title, j.status AS job_status,",
        "       a.stage, a.applied_at, a.updated_at",
        "FROM applications a",
        "JOIN jobs j ON j.id = a.job_id",
        "WHERE a.candidate_id = #{candidateId}",
        "ORDER BY a.updated_at DESC"
    })
    List<Map<String, Object>> listByCandidate(@Param("candidateId") Long candidateId);

    /** 是否已投递过 (job_id, candidate_id) ；唯一约束在 DB 也兜底，但提前检查 UX 更友好。 */
    @Select("SELECT COUNT(1) FROM applications WHERE job_id = #{jobId} AND candidate_id = #{candidateId}")
    long countDuplicate(@Param("jobId") Long jobId, @Param("candidateId") Long candidateId);

    // ─────────────────────────────────────────────────────────────
    //  M2++：多维筛选版本（jobIds 集合）。原 countByStage / listBoardItems 给 stats 继续用，不动签名。
    //  约定：jobIds 为 null → 不限制；为空集合 → 业务层应短路返回空（避免 SQL 报 syntax）。
    // ─────────────────────────────────────────────────────────────

    /**
     * 看板视图：按 stage 聚合一组岗位的投递数。jobIds == null 时不限制。
     * 调用方必须保证 jobIds 非空（空集合应在 service 层短路返回）。
     */
    @Select({
        "<script>",
        "SELECT a.stage AS stage, COUNT(*) AS cnt",
        "FROM applications a",
        "JOIN jobs j ON j.id = a.job_id",
        "WHERE j.deleted_at IS NULL",
        "<if test='jobIds != null'>",
        "  AND a.job_id IN <foreach collection='jobIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "</if>",
        "<if test='hrScope != null'>",
        "  AND ( j.created_by = #{hrScope.hrUserId}",
        "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
        "    OR j.sub_department_id IN",
        "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
        "  </if>",
        "  )",
        "</if>",
        "GROUP BY a.stage",
        "</script>"
    })
    List<Map<String, Object>> countByStageFiltered(@Param("jobIds") List<Long> jobIds,
                                                   @Param("hrScope") HrJobScope hrScope);

    /**
     * 看板每列明细：按 stage + jobIds 拉取候选人/岗位字段。
     * 调用方必须保证 jobIds 非空（空集合应在 service 层短路返回）。
     */
    @Select({
        "<script>",
        "SELECT a.id, a.job_id, j.title AS job_title, a.candidate_id,",
        "       u.full_name AS candidate_name, u.email AS candidate_email,",
        "       a.stage, a.applied_at, a.updated_at, a.years_exp",
        "FROM applications a",
        "JOIN jobs j  ON j.id = a.job_id",
        "JOIN users u ON u.id = a.candidate_id",
        "WHERE j.deleted_at IS NULL",
        "<if test='jobIds != null'>",
        "  AND a.job_id IN <foreach collection='jobIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "</if>",
        "<if test='hrScope != null'>",
        "  AND ( j.created_by = #{hrScope.hrUserId}",
        "  <if test='hrScope.subDepartmentIds != null and hrScope.subDepartmentIds.size() > 0'>",
        "    OR j.sub_department_id IN",
        "    <foreach collection='hrScope.subDepartmentIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>",
        "  </if>",
        "  )",
        "</if>",
        "<if test='stage != null'> AND a.stage = #{stage}::application_stage </if>",
        "ORDER BY a.updated_at DESC",
        "<if test='offset != null'> OFFSET #{offset} </if>",
        "<if test='limit != null'> LIMIT #{limit} </if>",
        "</script>"
    })
    List<Map<String, Object>> listBoardItemsFiltered(@Param("jobIds") List<Long> jobIds,
                                                     @Param("hrScope") HrJobScope hrScope,
                                                     @Param("stage") String stage,
                                                     @Param("offset") Integer offset,
                                                     @Param("limit") Integer limit);
}
