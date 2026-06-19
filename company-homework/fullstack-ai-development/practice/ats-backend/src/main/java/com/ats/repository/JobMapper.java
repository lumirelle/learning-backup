package com.ats.repository;

import com.ats.entity.Job;
import com.ats.job.HrJobScope;
import com.ats.job.dto.JobListReq;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobMapper extends BaseMapper<Job> {

    /** 候选人查看详情时 view_count 原子 +1。 */
    @Update("UPDATE jobs SET view_count = view_count + 1 WHERE id = #{id} AND deleted_at IS NULL")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 动态条件 + 分页查询（不含 description 之外的所有字段）。
     *
     * @param q                 前端传来的过滤参数
     * @param ownerOnlyUserId   非 null 时强制 WHERE created_by = ownerOnlyUserId（HR mine 模式）
     * @param visibleStatuses   非空时强制 WHERE status IN (...)（候选人/匿名时由 service 强制注入）
     * @param sortColumn        白名单：published_at / created_at / view_count / salary_max
     * @param sortDir           ASC / DESC
     * @param offset            分页 offset
     * @param size              分页 size
     */
    List<Job> listJobs(@Param("q") JobListReq q,
                       @Param("ownerOnlyUserId") Long ownerOnlyUserId,
                       @Param("hrScope") HrJobScope hrScope,
                       @Param("visibleStatuses") List<String> visibleStatuses,
                       @Param("sortColumn") String sortColumn,
                       @Param("sortDir") String sortDir,
                       @Param("offset") int offset,
                       @Param("size") int size);

    long countJobs(@Param("q") JobListReq q,
                   @Param("ownerOnlyUserId") Long ownerOnlyUserId,
                   @Param("hrScope") HrJobScope hrScope,
                   @Param("visibleStatuses") List<String> visibleStatuses);

    /**
     * 仅返回符合条件的 job IDs（不分页、不带 tag/dept/user join）。
     * 用途：HR 看板按多维过滤先求出"哪些岗位"，再聚合这些岗位的投递。
     * 复用 {@code <sql id="conditions">}，保证与 {@link #listJobs} 行为一致。
     */
    List<Long> selectFilteredJobIds(@Param("q") JobListReq q,
                                    @Param("ownerOnlyUserId") Long ownerOnlyUserId,
                                    @Param("hrScope") HrJobScope hrScope,
                                    @Param("visibleStatuses") List<String> visibleStatuses,
                                    @Param("limit") int limit);

    /** HR 可见范围内全部岗位 id（无额外 keyword 等过滤）。 */
    List<Long> selectJobIdsInHrScope(@Param("hrScope") HrJobScope hrScope,
                                     @Param("limit") int limit);

    // M6: 部门字典查询已迁出至 DepartmentMapper / SubDepartmentMapper（独立 entity 后语义更清晰）。
}
