package com.ats.job;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.security.SecurityUtil;
import com.ats.entity.Job;
import com.ats.entity.JobLevel;
import com.ats.entity.JobStatus;
import com.ats.entity.JobWorkType;
import com.ats.entity.Tag;
import com.ats.entity.TagCategory;
import com.ats.entity.User;
import com.ats.job.dto.JobCreateReq;
import com.ats.job.dto.JobDetailVO;
import com.ats.job.dto.JobListItemVO;
import com.ats.job.dto.JobListReq;
import com.ats.job.dto.JobUpdateReq;
import com.ats.job.dto.TagVO;
import com.ats.repository.JobMapper;
import com.ats.repository.JobTagMapper;
import com.ats.repository.JobTagRow;
import com.ats.repository.SubDepartmentMapper;
import com.ats.repository.TagMapper;
import com.ats.repository.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobMapper jobMapper;
    private final TagMapper tagMapper;
    private final JobTagMapper jobTagMapper;
    private final UserMapper userMapper;
    private final SubDepartmentMapper subDepartmentMapper;
    private final HrJobScopeService hrJobScopeService;

    /** sortBy 白名单：snake_case 列名 */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "publishedAt", "j.published_at",
            "createdAt",   "j.created_at",
            "viewCount",   "j.view_count",
            "salaryMax",   "j.salary_max"
    );

    /** 候选人 / 匿名 用户能看到的状态集合（HR 看自己的可看全部） */
    private static final List<String> CANDIDATE_VISIBLE = List.of(
            JobStatus.PUBLISHED.name(), JobStatus.PAUSED.name(), JobStatus.CLOSED.name());

    // ─────────────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public JobDetailVO create(JobCreateReq req) {
        Long currentUserId = SecurityUtil.requireUserId();
        validateSalaryRange(req.getSalaryMin(), req.getSalaryMax());
        validateTagIds(req.getTagIds());
        validateSubDepartmentExists(req.getSubDepartmentId());

        Job job = new Job();
        job.setCreatedBy(currentUserId);
        job.setTitle(req.getTitle().trim());
        job.setDescription(req.getDescription());
        job.setWorkType(req.getWorkType().name());
        job.setLevel(req.getLevel().name());
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setHeadcount(req.getHeadcount() != null ? req.getHeadcount() : (short) 1);
        job.setSubDepartmentId(req.getSubDepartmentId());
        job.setStatus(JobStatus.DRAFT.name());
        job.setViewCount(0);
        jobMapper.insert(job);

        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            jobTagMapper.batchInsert(job.getId(), req.getTagIds());
        }

        log.info("[JOB] create id={} title='{}' by user={}", job.getId(), job.getTitle(), currentUserId);
        return getDetail(job.getId());
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE （仅修改基础字段；状态变更走 transition）
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public JobDetailVO update(Long id, JobUpdateReq req) {
        Job job = loadActiveOrThrow(id);
        requireOwnerOrAdmin(job);

        Integer newSalaryMin = req.getSalaryMin() != null ? req.getSalaryMin() : job.getSalaryMin();
        Integer newSalaryMax = req.getSalaryMax() != null ? req.getSalaryMax() : job.getSalaryMax();
        validateSalaryRange(newSalaryMin, newSalaryMax);

        if (req.getTitle() != null)       job.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) job.setDescription(req.getDescription());
        if (req.getWorkType() != null)    job.setWorkType(req.getWorkType().name());
        if (req.getLevel() != null)       job.setLevel(req.getLevel().name());
        if (req.getSalaryMin() != null)   job.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null)   job.setSalaryMax(req.getSalaryMax());
        if (req.getHeadcount() != null)   job.setHeadcount(req.getHeadcount());
        if (req.getSubDepartmentId() != null) {
            validateSubDepartmentExists(req.getSubDepartmentId());
            job.setSubDepartmentId(req.getSubDepartmentId());
        }

        jobMapper.updateById(job);

        // 标签：null=不动，[]=清空，非空=全量替换
        if (req.getTagIds() != null) {
            validateTagIds(req.getTagIds());
            jobTagMapper.deleteByJobId(id);
            if (!req.getTagIds().isEmpty()) {
                jobTagMapper.batchInsert(id, req.getTagIds());
            }
        }

        log.info("[JOB] update id={} by user={}", id, SecurityUtil.currentUserIdOrNull());
        return getDetail(id);
    }

    // ─────────────────────────────────────────────────────────────
    //  TRANSITION （状态机推进，触发 published_at / closed_at）
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public JobDetailVO transition(Long id, JobStatus target) {
        Job job = loadActiveOrThrow(id);
        requireOwnerOrAdmin(job);

        JobStatus from = JobStatus.valueOf(job.getStatus());
        JobStatusMachine.requireTransition(from, target);

        job.setStatus(target.name());
        OffsetDateTime now = OffsetDateTime.now();
        if (target == JobStatus.PUBLISHED && job.getPublishedAt() == null) {
            job.setPublishedAt(now);
        }
        if (target == JobStatus.CLOSED && job.getClosedAt() == null) {
            job.setClosedAt(now);
        }
        jobMapper.updateById(job);

        log.info("[JOB] transition id={} {} → {} by user={}",
                id, from, target, SecurityUtil.currentUserIdOrNull());
        return getDetail(id);
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE （软删，Admin 专用）
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public void softDelete(Long id) {
        if (!SecurityUtil.isAdmin()) {
            throw BizException.of(ErrorCode.JOB_ACCESS_DENIED);
        }
        Job job = loadActiveOrThrow(id);
        jobMapper.deleteById(job.getId()); // @TableLogic 自动转 UPDATE SET deleted_at = NOW()
        log.info("[JOB] soft-delete id={} by admin={}", id, SecurityUtil.currentUserIdOrNull());
    }

    // ─────────────────────────────────────────────────────────────
    //  DETAIL
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public JobDetailVO getDetail(Long id) {
        Job job = loadActiveOrThrow(id);

        Long currentUserId = SecurityUtil.currentUserIdOrNull();
        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isOwner = currentUserId != null && currentUserId.equals(job.getCreatedBy());

        // 可见性：HR(own)/Admin 全状态可见；其他人只能看候选人 visible
        if (!isAdmin && !isOwner && !CANDIDATE_VISIBLE.contains(job.getStatus())) {
            throw BizException.of(ErrorCode.JOB_NOT_PUBLISHED);
        }

        // 候选人 / 匿名 浏览时 view_count +1（HR/Admin 看自己不计数，避免污染数据）
        if (!isAdmin && !isOwner) {
            jobMapper.incrementViewCount(id);
            job.setViewCount(job.getViewCount() + 1);
        }

        Map<Long, List<TagVO>> tagMap = batchLoadTags(List.of(id));
        Map<Long, User> userMap = batchLoadUsers(List.of(job.getCreatedBy()));
        Map<Long, SubDepartmentExpanded> subDeptMap = batchLoadSubDepartments(
                job.getSubDepartmentId() == null ? List.of() : List.of(job.getSubDepartmentId()));

        return toDetailVO(job, tagMap, userMap, subDeptMap, isAdmin || isOwner);
    }

    // ─────────────────────────────────────────────────────────────
    //  LIST （分页 + 动态过滤）
    // ─────────────────────────────────────────────────────────────

    public PageResult<JobListItemVO> list(JobListReq req) {
        Long currentUserId = SecurityUtil.currentUserIdOrNull();
        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isHr = SecurityUtil.isHr();

        // 权限决策：候选人 / 匿名 强制只看 PUBLISHED/PAUSED/CLOSED；
        // HR 默认看「自己创建的所有」+「他人 PUBLISHED/PAUSED/CLOSED」(由 ownerOnly 与 visibleStatuses 共同表达，但
        //   实际更简单：HR 不传 mine 时与候选人列表合并不太合理，所以这里强制：HR mine=true→只看自己；HR mine!=true→视为公开浏览)
        Long ownerOnlyUserId = null;
        HrJobScope hrScope = null;
        List<String> visibleStatuses = null;

        if (isAdmin) {
            if (Boolean.TRUE.equals(req.getMine())) ownerOnlyUserId = currentUserId;
        }
        else if (isHr) {
            if (Boolean.TRUE.equals(req.getMine())) {
                ownerOnlyUserId = currentUserId;
            } else if (Boolean.TRUE.equals(req.getTeam())) {
                hrScope = hrJobScopeService.currentScopeOrNull();
            } else {
                visibleStatuses = CANDIDATE_VISIBLE;
            }
        }
        else {
            // CANDIDATE / 匿名：mine 强制忽略；只看公开
            visibleStatuses = CANDIDATE_VISIBLE;
        }

        // 是否包含 ARCHIVED（仅对能看到归档的角色生效）
        if (Boolean.TRUE.equals(req.getIncludeArchived()) && visibleStatuses == null) {
            // 不限制 status
        } else if (visibleStatuses == null) {
            // ownerOnly 但不含 archived → 排除 ARCHIVED
            visibleStatuses = List.of(
                    JobStatus.DRAFT.name(),
                    JobStatus.PUBLISHED.name(),
                    JobStatus.PAUSED.name(),
                    JobStatus.CLOSED.name());
        }

        String sortColumn = SORT_COLUMNS.getOrDefault(req.getSortBy(), "j.published_at");
        String sortDir = "asc".equalsIgnoreCase(req.getSortOrder()) ? "ASC" : "DESC";
        int page = req.getPage() == null || req.getPage() < 1 ? 1 : req.getPage();
        int size = req.getSize() == null || req.getSize() < 1 ? 20 : Math.min(req.getSize(), 100);
        int offset = (page - 1) * size;

        long total = jobMapper.countJobs(req, ownerOnlyUserId, hrScope, visibleStatuses);
        List<Job> jobs = total == 0
                ? Collections.emptyList()
                : jobMapper.listJobs(req, ownerOnlyUserId, hrScope, visibleStatuses, sortColumn, sortDir, offset, size);

        List<Long> jobIds = jobs.stream().map(Job::getId).toList();
        Map<Long, List<TagVO>> tagMap = batchLoadTags(jobIds);

        Set<Long> userIds = jobs.stream().map(Job::getCreatedBy).collect(Collectors.toSet());
        Set<Long> subDeptIds = jobs.stream()
                .map(Job::getSubDepartmentId).filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = batchLoadUsers(new ArrayList<>(userIds));
        Map<Long, SubDepartmentExpanded> subDeptMap = batchLoadSubDepartments(new ArrayList<>(subDeptIds));

        List<JobListItemVO> items = jobs.stream()
                .map(j -> toListItemVO(j, tagMap, userMap, subDeptMap))
                .toList();

        return new PageResult<>(items, total, page, size);
    }

    // ─────────────────────────────────────────────────────────────
    //  Helper
    // ─────────────────────────────────────────────────────────────

    private Job loadActiveOrThrow(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) throw BizException.of(ErrorCode.JOB_NOT_FOUND);
        return job;
    }

    private void requireOwnerOrAdmin(Job job) {
        if (!hrJobScopeService.canManageJob(job)) {
            throw BizException.of(ErrorCode.JOB_ACCESS_DENIED);
        }
    }

    private void validateSalaryRange(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            throw BizException.of(ErrorCode.JOB_SALARY_RANGE_INVALID);
        }
    }

    private void validateTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        long count = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>().in(Tag::getId, tagIds));
        if (count != new HashSet<>(tagIds).size()) {
            throw BizException.of(ErrorCode.TAG_NOT_FOUND);
        }
    }

    private Map<Long, List<TagVO>> batchLoadTags(List<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) return Collections.emptyMap();
        List<JobTagRow> rows = tagMapper.findTagsByJobIds(jobIds);
        Map<Long, List<TagVO>> map = new HashMap<>();
        for (JobTagRow r : rows) {
            map.computeIfAbsent(r.getJobId(), k -> new ArrayList<>())
               .add(TagVO.builder()
                       .id(r.getId())
                       .slug(r.getSlug())
                       .name(r.getName())
                       .category(TagCategory.valueOf(r.getCategory()))
                       .build());
        }
        return map;
    }

    private Map<Long, User> batchLoadUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        List<User> users = userMapper.selectByIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    /** 校验子部门存在；不存在时抛 DEPARTMENT_NOT_FOUND。 */
    private void validateSubDepartmentExists(Long subDepartmentId) {
        if (subDepartmentId == null) {
            throw BizException.of(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        if (subDepartmentMapper.selectById(subDepartmentId) == null) {
            throw BizException.of(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
    }

    /**
     * 批量加载子部门 + 上层部门 + 根组织 展示信息。
     * 复用 SubDepartmentMapper.selectExpandedByIds 的 JOIN 结果。
     */
    private Map<Long, SubDepartmentExpanded> batchLoadSubDepartments(List<Long> subDeptIds) {
        if (subDeptIds == null || subDeptIds.isEmpty()) return Collections.emptyMap();
        return subDepartmentMapper.selectExpandedByIds(subDeptIds).stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("id")).longValue(),
                        SubDepartmentExpanded::from));
    }

    private static JobListItemVO toListItemVO(Job j,
                                              Map<Long, List<TagVO>> tagMap,
                                              Map<Long, User> userMap,
                                              Map<Long, SubDepartmentExpanded> subDeptMap) {
        List<TagVO> tags = tagMap.getOrDefault(j.getId(), Collections.emptyList());
        if (tags.size() > 5) tags = tags.subList(0, 5);
        User creator = userMap.get(j.getCreatedBy());
        SubDepartmentExpanded sd = j.getSubDepartmentId() == null ? null : subDeptMap.get(j.getSubDepartmentId());
        return JobListItemVO.builder()
                .id(j.getId())
                .title(j.getTitle())
                .location(sd == null ? null : sd.location)
                .workType(JobWorkType.valueOf(j.getWorkType()))
                .level(JobLevel.valueOf(j.getLevel()))
                .salaryMin(j.getSalaryMin())
                .salaryMax(j.getSalaryMax())
                .salaryRange(formatSalary(j.getSalaryMin(), j.getSalaryMax()))
                .headcount(j.getHeadcount())
                .status(JobStatus.valueOf(j.getStatus()))
                .viewCount(j.getViewCount())
                .publishedAt(j.getPublishedAt())
                .updatedAt(j.getUpdatedAt())
                .subDepartmentId(j.getSubDepartmentId())
                .subDepartmentName(sd == null ? null : sd.name)
                .departmentId(sd == null ? null : sd.parentDepartmentId)
                .departmentName(sd == null ? null : sd.parentDepartmentName)
                .rootOrgId(sd == null ? null : sd.rootOrgId)
                .rootOrgName(sd == null ? null : sd.rootOrgName)
                .createdBy(j.getCreatedBy())
                .createdByName(creator == null ? null : creator.getFullName())
                .tags(tags)
                .build();
    }

    private static JobDetailVO toDetailVO(Job j,
                                          Map<Long, List<TagVO>> tagMap,
                                          Map<Long, User> userMap,
                                          Map<Long, SubDepartmentExpanded> subDeptMap,
                                          boolean canManage) {
        User creator = userMap.get(j.getCreatedBy());
        JobStatus status = JobStatus.valueOf(j.getStatus());
        SubDepartmentExpanded sd = j.getSubDepartmentId() == null ? null : subDeptMap.get(j.getSubDepartmentId());
        return JobDetailVO.builder()
                .id(j.getId())
                .title(j.getTitle())
                .description(j.getDescription())
                .location(sd == null ? null : sd.location)
                .workType(JobWorkType.valueOf(j.getWorkType()))
                .level(JobLevel.valueOf(j.getLevel()))
                .salaryMin(j.getSalaryMin())
                .salaryMax(j.getSalaryMax())
                .salaryRange(formatSalary(j.getSalaryMin(), j.getSalaryMax()))
                .headcount(j.getHeadcount())
                .status(status)
                .viewCount(j.getViewCount())
                .publishedAt(j.getPublishedAt())
                .closedAt(j.getClosedAt())
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .subDepartmentId(j.getSubDepartmentId())
                .subDepartmentName(sd == null ? null : sd.name)
                .departmentId(sd == null ? null : sd.parentDepartmentId)
                .departmentName(sd == null ? null : sd.parentDepartmentName)
                .rootOrgId(sd == null ? null : sd.rootOrgId)
                .rootOrgName(sd == null ? null : sd.rootOrgName)
                .createdBy(j.getCreatedBy())
                .createdByName(creator == null ? null : creator.getFullName())
                .tags(tagMap.getOrDefault(j.getId(), Collections.emptyList()))
                .allowedTransitions(canManage ? JobStatusMachine.nextStates(status) : null)
                .build();
    }

    /** 内部容器：把 SubDepartmentMapper 返回的扁平行映射成可读字段，避免 Map 散落。 */
    private record SubDepartmentExpanded(
            Long id, String name, String location,
            Long parentDepartmentId, String parentDepartmentName,
            Long rootOrgId, String rootOrgName) {
        static SubDepartmentExpanded from(Map<String, Object> row) {
            return new SubDepartmentExpanded(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("name"),
                    (String) row.get("location"),
                    row.get("parent_department_id") == null ? null : ((Number) row.get("parent_department_id")).longValue(),
                    (String) row.get("parent_department_name"),
                    row.get("root_org_id") == null ? null : ((Number) row.get("root_org_id")).longValue(),
                    (String) row.get("root_org_name"));
        }
    }

    /** "30k-50k" / "30k 起" / "面议" */
    private static String formatSalary(Integer min, Integer max) {
        if (min == null && max == null) return "薪资面议";
        if (min != null && max == null)  return (min / 1000) + "k 起";
        if (min == null)                  return "面议 - " + (max / 1000) + "k";
        return (min / 1000) + "k - " + (max / 1000) + "k";
    }

    /** 简易分页结果包装 */
    public record PageResult<T>(List<T> items, long total, int page, int size) {}
}
