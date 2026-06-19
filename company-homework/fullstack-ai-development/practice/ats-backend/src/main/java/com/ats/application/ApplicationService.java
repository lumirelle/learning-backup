package com.ats.application;

import com.ats.application.dto.ApplicationCreateReq;
import com.ats.application.dto.ApplicationDetailVO;
import com.ats.application.dto.ApplicationListItemVO;
import com.ats.application.dto.BoardColumnVO;
import com.ats.application.dto.BoardQueryReq;
import com.ats.application.dto.BoardVO;
import com.ats.application.dto.StageLogVO;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.security.SecurityUtil;
import com.ats.entity.Application;
import com.ats.entity.ApplicationStage;
import com.ats.entity.Job;
import com.ats.entity.JobStatus;
import com.ats.entity.StageLog;
import com.ats.entity.User;
import com.ats.job.HrJobScope;
import com.ats.job.HrJobScopeService;
import com.ats.job.dto.JobListReq;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.JobMapper;
import com.ats.repository.StageLogMapper;
import com.ats.repository.SubDepartmentMapper;
import com.ats.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final StageLogMapper stageLogMapper;
    private final JobMapper jobMapper;
    private final UserMapper userMapper;
    private final SubDepartmentMapper subDepartmentMapper;
    private final HrJobScopeService hrJobScopeService;

    /**
     * 看板列固定按状态机顺序输出（即使某列计数为 0 也要返回空列），
     * 让前端不需要做"补齐缺失列"的工作。
     */
    private static final List<ApplicationStage> BOARD_ORDER = List.of(
            ApplicationStage.APPLIED,
            ApplicationStage.SCREENING_PASS,
            ApplicationStage.PHONE_INTERVIEW,
            ApplicationStage.TECH_INTERVIEW,
            ApplicationStage.HR_INTERVIEW,
            ApplicationStage.OFFER,
            ApplicationStage.HIRED,
            ApplicationStage.REJECTED
    );

    // ─────────────────────────────────────────────────────────────
    //  CREATE：候选人投递
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public ApplicationDetailVO apply(ApplicationCreateReq req) {
        Long candidateId = SecurityUtil.requireUserId();
        if (!SecurityUtil.isCandidate()) {
            throw new BizException(ErrorCode.FORBIDDEN, "只有候选人可以投递岗位");
        }

        Job job = jobMapper.selectById(req.getJobId());
        if (job == null) throw BizException.of(ErrorCode.JOB_NOT_FOUND);
        if (!JobStatus.PUBLISHED.name().equals(job.getStatus())) {
            throw BizException.of(ErrorCode.JOB_NOT_HIRING);
        }
        if (Objects.equals(job.getCreatedBy(), candidateId)) {
            // 防止自创自投（即便 ADMIN 自降为 CANDIDATE 这种边界）
            throw BizException.of(ErrorCode.SELF_APPLY_FORBIDDEN);
        }
        if (applicationMapper.countDuplicate(job.getId(), candidateId) > 0) {
            throw BizException.of(ErrorCode.DUPLICATE_APPLICATION);
        }

        Application app = new Application();
        app.setJobId(job.getId());
        app.setCandidateId(candidateId);
        app.setStage(ApplicationStage.APPLIED.name());
        app.setResumeUrl(req.getResumeUrl());
        app.setYearsExp(req.getYearsExp());
        app.setPhone(req.getPhone());
        applicationMapper.insert(app);

        // 初始 stage_log：from = null, to = APPLIED
        StageLog initLog = new StageLog();
        initLog.setApplicationId(app.getId());
        initLog.setOperatedBy(candidateId);
        initLog.setFromStage(null);
        initLog.setToStage(ApplicationStage.APPLIED.name());
        initLog.setNote("候选人投递");
        stageLogMapper.insert(initLog);

        log.info("[APP] apply id={} job={} candidate={}", app.getId(), job.getId(), candidateId);
        return getDetail(app.getId());
    }

    // ─────────────────────────────────────────────────────────────
    //  TRANSITION：HR / Admin 推进阶段
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public ApplicationDetailVO transition(Long applicationId, ApplicationStage target, String note) {
        Long currentUserId = SecurityUtil.requireUserId();
        Application app = loadOrThrow(applicationId);
        Job job = jobMapper.selectById(app.getJobId());
        if (job == null) throw BizException.of(ErrorCode.JOB_NOT_FOUND);

        if (!hrJobScopeService.canManageJob(job)) {
            throw BizException.of(ErrorCode.APPLICATION_ACCESS_DENIED);
        }

        ApplicationStage from = ApplicationStage.valueOf(app.getStage());
        ApplicationStageMachine.requireTransition(from, target);

        // REJECTED 必须填原因
        if (target == ApplicationStage.REJECTED && (note == null || note.isBlank())) {
            throw BizException.of(ErrorCode.REJECT_REASON_REQUIRED);
        }

        app.setStage(target.name());
        if (target == ApplicationStage.REJECTED) {
            app.setRejectReason(note);
        }
        applicationMapper.updateById(app);

        StageLog logRow = new StageLog();
        logRow.setApplicationId(app.getId());
        logRow.setOperatedBy(currentUserId);
        logRow.setFromStage(from.name());
        logRow.setToStage(target.name());
        logRow.setNote(note);
        stageLogMapper.insert(logRow);

        log.info("[APP] transition id={} {} → {} by user={}", applicationId, from, target, currentUserId);
        return getDetail(applicationId);
    }

    // ─────────────────────────────────────────────────────────────
    //  DETAIL
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApplicationDetailVO getDetail(Long applicationId) {
        Long currentUserId = SecurityUtil.currentUserIdOrNull();
        Application app = loadOrThrow(applicationId);
        Job job = jobMapper.selectById(app.getJobId());
        if (job == null) throw BizException.of(ErrorCode.JOB_NOT_FOUND);

        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isOwnerCandidate = Objects.equals(currentUserId, app.getCandidateId());
        boolean canManage = hrJobScopeService.canManageJob(job);

        if (!isAdmin && !isOwnerCandidate && !canManage) {
            throw BizException.of(ErrorCode.APPLICATION_ACCESS_DENIED);
        }

        // 候选人姓名 / 邮箱
        String candidateName = null;
        String candidateEmail = null;
        if (app.getCandidateId() != null) {
            User candidate = userMapper.selectById(app.getCandidateId());
            if (candidate != null) {
                candidateName = candidate.getFullName();
                candidateEmail = candidate.getEmail();
            }
        }

        List<StageLogVO> logs = stageLogMapper.findByApplicationId(applicationId).stream()
                .map(ApplicationService::toStageLogVO)
                .toList();

        ApplicationStage stage = ApplicationStage.valueOf(app.getStage());

        ApplicationDetailVO.ApplicationDetailVOBuilder voBuilder = ApplicationDetailVO.builder()
                .id(app.getId())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .jobStatus(JobStatus.valueOf(job.getStatus()));
        enrichJobOrganization(job, voBuilder);

        return voBuilder
                .candidateId(app.getCandidateId())
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .stage(stage)
                .resumeUrl(app.getResumeUrl())
                .yearsExp(app.getYearsExp())
                // 候选人不必将 phone / rejectReason 暴露给非本人 / 非管理者；这里 owner 与 manager 都能看
                .phone(canManage || isOwnerCandidate ? app.getPhone() : null)
                .rejectReason(app.getRejectReason())
                .appliedAt(app.getAppliedAt())
                .updatedAt(app.getUpdatedAt())
                .stageLogs(logs)
                .allowedTransitions(canManage
                        ? ApplicationStageMachine.nextStages(stage)
                        : null)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  LIST：候选人「我的投递」
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ApplicationListItemVO> listMine() {
        Long candidateId = SecurityUtil.requireUserId();
        if (!SecurityUtil.isCandidate()) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅候选人可访问「我的投递」");
        }
        return applicationMapper.listByCandidate(candidateId).stream().map(row -> {
            String stageStr = (String) row.get("stage");
            String jobStatusStr = (String) row.get("job_status");
            return ApplicationListItemVO.builder()
                    .id(((Number) row.get("id")).longValue())
                    .jobId(((Number) row.get("job_id")).longValue())
                    .jobTitle((String) row.get("job_title"))
                    .jobStatus(jobStatusStr == null ? null : JobStatus.valueOf(jobStatusStr))
                    .candidateId(candidateId)
                    .stage(ApplicationStage.valueOf(stageStr))
                    .appliedAt(toOffsetDateTime(row.get("applied_at")))
                    .updatedAt(toOffsetDateTime(row.get("updated_at")))
                    .build();
        }).toList();
    }

    // ─────────────────────────────────────────────────────────────
    //  BOARD：HR / Admin 看板
    // ─────────────────────────────────────────────────────────────

    /** 看板候选岗位 IDs 求解上限：超过会被截断（前端的过滤越精准，越不会触顶）。 */
    private static final int BOARD_JOB_IDS_LIMIT = 500;

    /**
     * 旧签名（保留向后兼容 + 测试覆盖）：单 jobId / 全局聚合（看 HR 名下 / Admin 全平台）。
     * 内部委托新 {@link #board(BoardQueryReq)}。
     */
    @Transactional(readOnly = true)
    public BoardVO board(Long jobId, int itemsPerColumn) {
        BoardQueryReq req = new BoardQueryReq();
        req.setJobId(jobId);
        req.setItemsPerColumn(itemsPerColumn);
        return board(req);
    }

    /**
     * 多维筛选看板：
     * <ul>
     *   <li>{@code jobId} 给定 → 单岗位看板（保留 owner/admin 鉴权）</li>
     *   <li>否则按 keyword / workType / level / departmentId / location / salary / tagSlugs 过滤岗位，
     *       再聚合这些岗位下的投递。HR 自动限制为"自己名下岗位"；Admin 无限制</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public BoardVO board(BoardQueryReq req) {
        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isHr = SecurityUtil.isHr();
        if (!isAdmin && !isHr) {
            throw BizException.of(ErrorCode.FORBIDDEN);
        }

        Long jobId = req.getJobId();
        int itemsPerColumn = req.getItemsPerColumn() == null ? 50 : req.getItemsPerColumn();
        int limit = itemsPerColumn <= 0 ? 50 : Math.min(itemsPerColumn, 200);
        int offset = req.getColumnOffset() == null || req.getColumnOffset() < 0 ? 0 : req.getColumnOffset();
        HrJobScope hrScope = isAdmin ? null : hrJobScopeService.currentScopeOrNull();

        // ── 单岗位看板 ──
        if (jobId != null) {
            Job job = jobMapper.selectById(jobId);
            if (job == null) throw BizException.of(ErrorCode.JOB_NOT_FOUND);
            if (!hrJobScopeService.canManageJob(job)) {
                throw BizException.of(ErrorCode.JOB_ACCESS_DENIED);
            }
            return buildBoard(List.of(jobId), null, jobId, job.getTitle(), limit, offset, req.getStage(), false);
        }

        boolean hasAnyFilter = hasAnyFilter(req);
        List<Long> jobIds = null;
        boolean jobsTruncated = false;
        if (hasAnyFilter) {
            JobListReq jobReq = toJobListReq(req);
            List<String> visibleStatuses = null;
            jobIds = jobMapper.selectFilteredJobIds(jobReq, null, hrScope, visibleStatuses, BOARD_JOB_IDS_LIMIT + 1);
            if (jobIds.size() > BOARD_JOB_IDS_LIMIT) {
                jobsTruncated = true;
                jobIds = jobIds.subList(0, BOARD_JOB_IDS_LIMIT);
            }
            if (jobIds.isEmpty()) {
                return emptyBoard();
            }
        }

        HrJobScope appScope = jobIds != null ? null : hrScope;
        BoardVO vo = buildBoard(jobIds, appScope, null, null, limit, offset, req.getStage(), jobsTruncated);
        vo.setJobsTruncated(jobsTruncated);
        return vo;
    }

    /**
     * 公共看板组装。
     * jobIds 非 null 时岗位已预筛选，appScope 传 null；否则用 appScope 裁剪 HR 可见岗位。
     */
    private BoardVO buildBoard(List<Long> jobIds,
                               HrJobScope appScope,
                               Long singleJobId,
                               String singleJobTitle,
                               int limit,
                               int offset,
                               ApplicationStage onlyStage,
                               boolean jobsTruncated) {
        Map<ApplicationStage, Long> counts = new LinkedHashMap<>();
        for (Map<String, Object> row : applicationMapper.countByStageFiltered(jobIds, appScope)) {
            counts.put(
                    ApplicationStage.valueOf((String) row.get("stage")),
                    ((Number) row.get("cnt")).longValue());
        }

        List<ApplicationStage> stages = onlyStage != null ? List.of(onlyStage) : BOARD_ORDER;
        List<BoardColumnVO> columns = new ArrayList<>(stages.size());
        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        for (ApplicationStage st : stages) {
            long count = counts.getOrDefault(st, 0L);
            List<Map<String, Object>> rows = count == 0
                    ? List.of()
                    : applicationMapper.listBoardItemsFiltered(
                            jobIds, appScope, st.name(), offset > 0 ? offset : null, limit);
            List<ApplicationListItemVO> items = rows.stream()
                    .map(ApplicationService::toListItemVO)
                    .toList();
            columns.add(BoardColumnVO.builder()
                    .stage(st)
                    .count(count)
                    .items(items)
                    .hasMore(count > offset + items.size())
                    .build());
        }

        return BoardVO.builder()
                .jobId(singleJobId)
                .jobTitle(singleJobTitle)
                .columns(columns)
                .totalApplications(total)
                .jobsTruncated(jobsTruncated)
                .build();
    }

    private BoardVO emptyBoard() {
        List<BoardColumnVO> columns = BOARD_ORDER.stream()
                .map(st -> BoardColumnVO.builder().stage(st).count(0L).items(List.of()).hasMore(false).build())
                .toList();
        return BoardVO.builder()
                .jobId(null)
                .jobTitle(null)
                .columns(columns)
                .totalApplications(0L)
                .jobsTruncated(false)
                .build();
    }

    /** 看板 query → JobListReq 子集映射（只用过滤字段，不涉及分页/排序）。 */
    private static JobListReq toJobListReq(BoardQueryReq req) {
        JobListReq j = new JobListReq();
        j.setKeyword(req.getKeyword());
        j.setWorkType(req.getWorkType());
        j.setLevel(req.getLevel());
        j.setTagSlugs(req.getTagSlugs());
        j.setDepartmentId(req.getDepartmentId());
        j.setSubDepartmentId(req.getSubDepartmentId());
        j.setLocation(req.getLocation());
        j.setSalaryMin(req.getSalaryMin());
        j.setSalaryMax(req.getSalaryMax());
        return j;
    }

    /** 是否传了至少一个"筛选字段"（jobId 与 itemsPerColumn 不算）。 */
    private static boolean hasAnyFilter(BoardQueryReq req) {
        return isNotBlank(req.getKeyword())
                || isNotBlank(req.getLocation())
                || req.getDepartmentId() != null
                || req.getSubDepartmentId() != null
                || req.getSalaryMin() != null
                || req.getSalaryMax() != null
                || (req.getWorkType() != null && !req.getWorkType().isEmpty())
                || (req.getLevel() != null && !req.getLevel().isEmpty())
                || (req.getTagSlugs() != null && !req.getTagSlugs().isEmpty());
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** 从 sub_department 展开行填充组织三段 + 工作地点（与 JobDetailVO 语义对齐）。 */
    private void enrichJobOrganization(Job job, ApplicationDetailVO.ApplicationDetailVOBuilder builder) {
        if (job.getSubDepartmentId() == null) {
            return;
        }
        List<Map<String, Object>> rows = subDepartmentMapper.selectExpandedByIds(List.of(job.getSubDepartmentId()));
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Object> row = rows.get(0);
        builder.subDepartmentId(((Number) row.get("id")).longValue())
                .subDepartmentName((String) row.get("name"))
                .jobLocation((String) row.get("location"));
        if (row.get("parent_department_id") != null) {
            builder.departmentId(((Number) row.get("parent_department_id")).longValue())
                    .departmentName((String) row.get("parent_department_name"));
        }
        if (row.get("root_org_id") != null) {
            builder.rootOrgId(((Number) row.get("root_org_id")).longValue())
                    .rootOrgName((String) row.get("root_org_name"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private Application loadOrThrow(Long id) {
        Application app = applicationMapper.selectById(id);
        if (app == null) throw BizException.of(ErrorCode.APPLICATION_NOT_FOUND);
        return app;
    }

    /**
     * 把 Map&lt;String,Object&gt; 里的时间字段安全转为 OffsetDateTime。
     * <p>
     * 走原生 SQL 用 Map 接住时，PG <code>timestamptz</code> 在 JDBC 层默认是 java.sql.Timestamp
     * （不会自动转成 java.time.OffsetDateTime），强转必抛 ClassCastException。
     * 同时兼容 LocalDateTime / OffsetDateTime / null 三种返回路径，保持前向兼容。
     */
    private static OffsetDateTime toOffsetDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof OffsetDateTime odt) return odt;
        if (v instanceof Timestamp ts) {
            return ts.toLocalDateTime().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (v instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (v instanceof java.util.Date d) {
            return d.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        throw new IllegalStateException("不支持的时间类型: " + v.getClass());
    }

    private static ApplicationListItemVO toListItemVO(Map<String, Object> row) {
        String stageStr = (String) row.get("stage");
        Number yearsExp = (Number) row.get("years_exp");
        return ApplicationListItemVO.builder()
                .id(((Number) row.get("id")).longValue())
                .jobId(((Number) row.get("job_id")).longValue())
                .jobTitle((String) row.get("job_title"))
                .candidateId(((Number) row.get("candidate_id")).longValue())
                .candidateName((String) row.get("candidate_name"))
                .candidateEmail((String) row.get("candidate_email"))
                .stage(ApplicationStage.valueOf(stageStr))
                .yearsExp(yearsExp == null ? null : yearsExp.shortValue())
                .appliedAt(toOffsetDateTime(row.get("applied_at")))
                .updatedAt(toOffsetDateTime(row.get("updated_at")))
                .build();
    }

    private static StageLogVO toStageLogVO(Map<String, Object> row) {
        String fromStr = (String) row.get("from_stage");
        String toStr = (String) row.get("to_stage");
        Number opBy = (Number) row.get("operated_by");
        return StageLogVO.builder()
                .id(((Number) row.get("id")).longValue())
                .fromStage(fromStr == null ? null : ApplicationStage.valueOf(fromStr))
                .toStage(ApplicationStage.valueOf(toStr))
                .note((String) row.get("note"))
                .operatedBy(opBy == null ? null : opBy.longValue())
                .operatedByName((String) row.get("operated_by_name"))
                .operatedByRole((String) row.get("operated_by_role"))
                .operatedAt(toOffsetDateTime(row.get("operated_at")))
                .build();
    }
}
