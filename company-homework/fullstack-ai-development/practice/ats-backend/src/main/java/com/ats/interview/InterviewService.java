package com.ats.interview;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.security.SecurityUtil;
import com.ats.entity.Application;
import com.ats.entity.InterviewConclusion;
import com.ats.entity.InterviewRecord;
import com.ats.entity.Job;
import com.ats.interview.dto.InterviewCreateReq;
import com.ats.interview.dto.InterviewUpdateReq;
import com.ats.interview.dto.InterviewVO;
import com.ats.job.HrJobScopeService;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.InterviewMapper;
import com.ats.repository.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 面试记录 service · M4。
 *
 * <h3>访问权（与 ApplicationService 保持一致）</h3>
 * <ul>
 *   <li>读 / 写：仅 ADMIN 或 该 application 对应岗位的 owner HR</li>
 *   <li>候选人不可访问（面试评价对候选人不可见，由前端不暴露入口 + 后端权限双层防御）</li>
 * </ul>
 *
 * <h3>编辑窗口</h3>
 * <ul>
 *   <li>面试官本人：{@code created_at} 距今 ≤ 24h 可改</li>
 *   <li>ADMIN：不受时间限制（用于人工修复脏数据 / 申诉）</li>
 *   <li>其他 HR（哪怕是 owner HR 但非作者）不能改 —— 谁面试谁负责</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;
    private final JobMapper jobMapper;
    private final HrJobScopeService hrJobScopeService;

    /** 编辑窗口 24 小时（业务约束，硬编码常量便于测试 / 调整） */
    private static final long EDIT_WINDOW_HOURS = 24;

    // ─────────────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public InterviewVO add(Long applicationId, InterviewCreateReq req) {
        Long currentUserId = SecurityUtil.requireUserId();
        ensureCanManage(applicationId, currentUserId);

        InterviewRecord r = new InterviewRecord();
        r.setApplicationId(applicationId);
        r.setInterviewerId(currentUserId);
        r.setRound(req.getRound());
        r.setRating(req.getRating());
        r.setConclusion(req.getConclusion().name());
        r.setStrengths(req.getStrengths());
        r.setWeaknesses(req.getWeaknesses());
        r.setNotes(req.getNotes());
        interviewMapper.insert(r);

        log.info("[IV] add id={} app={} interviewer={} round='{}' conclusion={}",
                r.getId(), applicationId, currentUserId, req.getRound(), req.getConclusion());

        // 重新查（拿 join 后的 interviewer name + role），避免再 select user
        return loadOne(r.getId(), currentUserId);
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public InterviewVO update(Long id, InterviewUpdateReq req) {
        Long currentUserId = SecurityUtil.requireUserId();

        InterviewRecord r = interviewMapper.selectById(id);
        if (r == null) throw BizException.of(ErrorCode.INTERVIEW_NOT_FOUND);

        // 1) 必须对所属 application 有管理权（防止跨岗位 HR 串改）
        ensureCanManage(r.getApplicationId(), currentUserId);

        // 2) 编辑窗口 + 作者校验
        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isAuthor = Objects.equals(r.getInterviewerId(), currentUserId);

        if (!isAdmin) {
            if (!isAuthor) throw BizException.of(ErrorCode.INTERVIEW_EDIT_FORBIDDEN);
            if (isOutsideEditWindow(r.getCreatedAt())) {
                throw BizException.of(ErrorCode.INTERVIEW_EDIT_EXPIRED);
            }
        }

        r.setRound(req.getRound());
        r.setRating(req.getRating());
        r.setConclusion(req.getConclusion().name());
        r.setStrengths(req.getStrengths());
        r.setWeaknesses(req.getWeaknesses());
        r.setNotes(req.getNotes());
        interviewMapper.updateById(r);

        log.info("[IV] update id={} by={} (admin={})", id, currentUserId, isAdmin);
        return loadOne(id, currentUserId);
    }

    // ─────────────────────────────────────────────────────────────
    //  LIST
    // ─────────────────────────────────────────────────────────────

    public List<InterviewVO> listByApplication(Long applicationId) {
        Long currentUserId = SecurityUtil.requireUserId();
        ensureCanManage(applicationId, currentUserId);

        List<Map<String, Object>> rows = interviewMapper.listByApplication(applicationId);
        return rows.stream()
                .map(row -> toVO(row, currentUserId, SecurityUtil.isAdmin()))
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    //  helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * 共享访问校验：当前用户必须是 ADMIN 或该 application 所属岗位的 owner HR。
     * <p>未通过统一抛 {@link ErrorCode#APPLICATION_ACCESS_DENIED}，复用既有错误码。</p>
     */
    private void ensureCanManage(Long applicationId, Long currentUserId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) throw BizException.of(ErrorCode.APPLICATION_NOT_FOUND);
        Job job = jobMapper.selectById(app.getJobId());
        if (job == null) throw BizException.of(ErrorCode.JOB_NOT_FOUND);

        if (!hrJobScopeService.canManageJob(job)) {
            throw BizException.of(ErrorCode.APPLICATION_ACCESS_DENIED);
        }
    }

    private InterviewVO loadOne(Long id, Long currentUserId) {
        // 复用 listByApplication 的结构：先拿 application_id，再 list，再过滤
        // 简化版：直接 selectById + 单条 join 太重，这里用一致的 list 路径取出后过滤
        InterviewRecord r = interviewMapper.selectById(id);
        if (r == null) throw BizException.of(ErrorCode.INTERVIEW_NOT_FOUND);
        return interviewMapper.listByApplication(r.getApplicationId()).stream()
                .filter(row -> ((Number) row.get("id")).longValue() == id)
                .findFirst()
                .map(row -> toVO(row, currentUserId, SecurityUtil.isAdmin()))
                .orElseThrow(() -> BizException.of(ErrorCode.INTERVIEW_NOT_FOUND));
    }

    /**
     * 编辑窗口判定：{@code createdAt} 距今超过 {@link #EDIT_WINDOW_HOURS} 即冻结。
     */
    static boolean isOutsideEditWindow(OffsetDateTime createdAt) {
        if (createdAt == null) return false; // 极端情况下数据缺失，按可编辑放过（让作者校验兜底）
        return ChronoUnit.HOURS.between(createdAt, OffsetDateTime.now()) >= EDIT_WINDOW_HOURS;
    }

    /**
     * 把 join 后的 Map row 映射成 VO。
     * <p>{@code editable} 字段在这里一并算好：交给前端控制按钮显隐，零额外往返。</p>
     */
    static InterviewVO toVO(Map<String, Object> row, Long currentUserId, boolean isAdmin) {
        Long id = ((Number) row.get("id")).longValue();
        Long applicationId = ((Number) row.get("application_id")).longValue();
        Number interviewerNum = (Number) row.get("interviewer_id");
        Long interviewerId = interviewerNum == null ? null : interviewerNum.longValue();

        String conclusionStr = (String) row.get("conclusion");
        InterviewConclusion conclusion = conclusionStr == null ? null : InterviewConclusion.valueOf(conclusionStr);

        Number ratingNum = (Number) row.get("rating");
        Short rating = ratingNum == null ? null : ratingNum.shortValue();

        OffsetDateTime createdAt = toOffsetDateTime(row.get("created_at"));
        OffsetDateTime updatedAt = toOffsetDateTime(row.get("updated_at"));

        boolean isAuthor = Objects.equals(interviewerId, currentUserId);
        boolean editable = isAdmin || (isAuthor && !isOutsideEditWindow(createdAt));

        return InterviewVO.builder()
                .id(id)
                .applicationId(applicationId)
                .interviewerId(interviewerId)
                .interviewerName((String) row.get("interviewer_name"))
                .interviewerRole((String) row.get("interviewer_role"))
                .round((String) row.get("round"))
                .rating(rating)
                .strengths((String) row.get("strengths"))
                .weaknesses((String) row.get("weaknesses"))
                .conclusion(conclusion)
                .notes((String) row.get("notes"))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .editable(editable)
                .build();
    }

    /**
     * 与 {@code ApplicationService.toOffsetDateTime} 同源 —— 走原生 SQL Map 的 PG timestamptz
     * 在 JDBC 默认 typeHandler 路径下是 {@link Timestamp}，必须 helper 转换避免 ClassCastException
     * （M3 踩坑，沉淀至 SKILL §2.7.11）。
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
}
