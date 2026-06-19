package com.ats.application;

import com.ats.application.dto.ApplicationCreateReq;
import com.ats.application.dto.ApplicationDetailVO;
import com.ats.application.dto.BoardVO;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.Application;
import com.ats.entity.ApplicationStage;
import com.ats.entity.Job;
import com.ats.entity.JobLevel;
import com.ats.entity.JobStatus;
import com.ats.entity.JobWorkType;
import com.ats.entity.StageLog;
import com.ats.entity.User;
import com.ats.job.HrJobScope;
import com.ats.job.HrJobScopeService;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.JobMapper;
import com.ats.repository.StageLogMapper;
import com.ats.repository.SubDepartmentMapper;
import com.ats.repository.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ApplicationService 纯 Mockito 单测，覆盖：
 *  · apply       ：happy / job 不存在 / 非 PUBLISHED / 重复 / 自创自投
 *  · transition  ：合法 / 非法 / 终态 / 拒绝缺 note / 非 owner HR
 *  · getDetail   ：候选人本人 / 候选人偷看别人 / HR(job owner) 看 allowedTransitions
 *  · listMine    ：基本流转
 *  · board       ：Admin / HR 越权
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApplicationService · 业务分支覆盖")
class ApplicationServiceTest {

    @Mock ApplicationMapper applicationMapper;
    @Mock StageLogMapper stageLogMapper;
    @Mock JobMapper jobMapper;
    @Mock UserMapper userMapper;
    @Mock SubDepartmentMapper subDepartmentMapper;
    @Mock HrJobScopeService hrJobScopeService;

    @InjectMocks ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        when(hrJobScopeService.canManageJob(any())).thenAnswer(inv -> {
            Job job = inv.getArgument(0);
            if (job == null) return false;
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
            if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"))) {
                return false;
            }
            return Objects.equals(Long.parseLong(auth.getName()), job.getCreatedBy());
        });
        when(hrJobScopeService.currentScopeOrNull()).thenAnswer(inv -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_HR"))) {
                return null;
            }
            return new HrJobScope(Long.parseLong(auth.getName()), List.of());
        });
        // 默认 stage logs / users 查空，避免 toDetailVO 路径出错
        when(stageLogMapper.findByApplicationId(anyLong())).thenReturn(Collections.emptyList());
        when(userMapper.selectById(anyLong())).thenReturn(makeUser(99L, "Candidate Cathy", "cathy@example.com", "CANDIDATE"));
        when(subDepartmentMapper.selectExpandedByIds(any())).thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ═════════════════════════════ apply ═════════════════════════════

    @Nested
    @DisplayName("apply · 投递")
    class Apply {

        @Test
        @DisplayName("happy: CANDIDATE 投 PUBLISHED 岗位 → insert app + 初始 stage_log(APPLIED)")
        void happyPath() {
            setAuth(99L, "CANDIDATE");
            Job job = loadedJob(1L, 10L, JobStatus.PUBLISHED);
            when(jobMapper.selectById(1L)).thenReturn(job);
            when(applicationMapper.countDuplicate(1L, 99L)).thenReturn(0L);
            doAnswerSetAppId(applicationMapper, 500L);
            when(applicationMapper.selectById(500L)).thenReturn(loadedApp(500L, 1L, 99L, ApplicationStage.APPLIED));

            ApplicationCreateReq req = new ApplicationCreateReq();
            req.setJobId(1L);
            req.setYearsExp((short) 3);
            req.setPhone("13800000000");

            ApplicationDetailVO vo = applicationService.apply(req);

            assertThat(vo.getStage()).isEqualTo(ApplicationStage.APPLIED);
            verify(applicationMapper).insert(any(Application.class));

            // 初始 stage_log: from=null, to=APPLIED
            ArgumentCaptor<StageLog> logCap = ArgumentCaptor.forClass(StageLog.class);
            verify(stageLogMapper).insert(logCap.capture());
            assertThat(logCap.getValue().getFromStage()).isNull();
            assertThat(logCap.getValue().getToStage()).isEqualTo("APPLIED");
            assertThat(logCap.getValue().getOperatedBy()).isEqualTo(99L);
        }

        @Test
        @DisplayName("岗位不存在 → JOB_NOT_FOUND")
        void jobNotFound() {
            setAuth(99L, "CANDIDATE");
            when(jobMapper.selectById(404L)).thenReturn(null);

            ApplicationCreateReq req = new ApplicationCreateReq();
            req.setJobId(404L);

            assertThatThrownBy(() -> applicationService.apply(req))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_NOT_FOUND));
        }

        @Test
        @DisplayName("岗位不是 PUBLISHED（DRAFT）→ JOB_NOT_HIRING")
        void notHiring() {
            setAuth(99L, "CANDIDATE");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.DRAFT));

            ApplicationCreateReq req = new ApplicationCreateReq();
            req.setJobId(1L);

            assertThatThrownBy(() -> applicationService.apply(req))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_NOT_HIRING));

            verify(applicationMapper, never()).insert(any(Application.class));
        }

        @Test
        @DisplayName("候选人是岗位创建人 → SELF_APPLY_FORBIDDEN")
        void selfApply() {
            setAuth(10L, "CANDIDATE"); // candidate id == job createdBy
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            ApplicationCreateReq req = new ApplicationCreateReq();
            req.setJobId(1L);

            assertThatThrownBy(() -> applicationService.apply(req))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.SELF_APPLY_FORBIDDEN));
        }

        @Test
        @DisplayName("重复投递 → DUPLICATE_APPLICATION")
        void duplicate() {
            setAuth(99L, "CANDIDATE");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));
            when(applicationMapper.countDuplicate(1L, 99L)).thenReturn(1L);

            ApplicationCreateReq req = new ApplicationCreateReq();
            req.setJobId(1L);

            assertThatThrownBy(() -> applicationService.apply(req))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.DUPLICATE_APPLICATION));
        }
    }

    // ═════════════════════════════ transition ═════════════════════════════

    @Nested
    @DisplayName("transition · 阶段流转")
    class Transition {

        @Test
        @DisplayName("HR(job owner) APPLIED → SCREENING_PASS：合法 + 写 stage_log + 更新 stage")
        void happyPath() {
            setAuth(10L, "HR");
            Application app = loadedApp(500L, 1L, 99L, ApplicationStage.APPLIED);
            when(applicationMapper.selectById(500L)).thenReturn(app);
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            applicationService.transition(500L, ApplicationStage.SCREENING_PASS, "简历看过");

            assertThat(app.getStage()).isEqualTo("SCREENING_PASS");
            verify(applicationMapper).updateById(any(Application.class));

            ArgumentCaptor<StageLog> cap = ArgumentCaptor.forClass(StageLog.class);
            verify(stageLogMapper).insert(cap.capture());
            assertThat(cap.getValue().getFromStage()).isEqualTo("APPLIED");
            assertThat(cap.getValue().getToStage()).isEqualTo("SCREENING_PASS");
            assertThat(cap.getValue().getOperatedBy()).isEqualTo(10L);
            assertThat(cap.getValue().getNote()).isEqualTo("简历看过");
        }

        @Test
        @DisplayName("APPLIED → OFFER：跳级非法 → ILLEGAL_TRANSITION，不写日志")
        void illegalTransition() {
            setAuth(10L, "HR");
            when(applicationMapper.selectById(500L)).thenReturn(loadedApp(500L, 1L, 99L, ApplicationStage.APPLIED));
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> applicationService.transition(500L, ApplicationStage.OFFER, null))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ILLEGAL_TRANSITION));

            verify(stageLogMapper, never()).insert(any(StageLog.class));
        }

        @Test
        @DisplayName("HIRED → 任意：终态 → APPLICATION_TERMINATED")
        void terminalCannotMove() {
            setAuth(10L, "HR");
            when(applicationMapper.selectById(500L)).thenReturn(loadedApp(500L, 1L, 99L, ApplicationStage.HIRED));
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> applicationService.transition(500L, ApplicationStage.OFFER, "重新发 offer"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.APPLICATION_TERMINATED));
        }

        @Test
        @DisplayName("流转到 REJECTED 但 note 为空 → REJECT_REASON_REQUIRED")
        void rejectMissingReason() {
            setAuth(10L, "HR");
            Application app = loadedApp(500L, 1L, 99L, ApplicationStage.SCREENING_PASS);
            when(applicationMapper.selectById(500L)).thenReturn(app);
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> applicationService.transition(500L, ApplicationStage.REJECTED, "  "))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.REJECT_REASON_REQUIRED));

            verify(applicationMapper, never()).updateById(any(Application.class));
        }

        @Test
        @DisplayName("REJECTED 流转 + 填了 note：写 reject_reason 字段")
        void rejectWithReason() {
            setAuth(10L, "HR");
            Application app = loadedApp(500L, 1L, 99L, ApplicationStage.PHONE_INTERVIEW);
            when(applicationMapper.selectById(500L)).thenReturn(app);
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            applicationService.transition(500L, ApplicationStage.REJECTED, "技术不达标");

            assertThat(app.getStage()).isEqualTo("REJECTED");
            assertThat(app.getRejectReason()).isEqualTo("技术不达标");
        }

        @Test
        @DisplayName("非 job-owner HR → APPLICATION_ACCESS_DENIED")
        void nonOwnerHr() {
            setAuth(20L, "HR"); // 当前 HR id=20，但 job createdBy=10
            when(applicationMapper.selectById(500L)).thenReturn(loadedApp(500L, 1L, 99L, ApplicationStage.APPLIED));
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> applicationService.transition(500L, ApplicationStage.SCREENING_PASS, null))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.APPLICATION_ACCESS_DENIED));
        }
    }

    // ═════════════════════════════ getDetail ═════════════════════════════

    @Nested
    @DisplayName("getDetail · 详情可见性")
    class GetDetail {

        @Test
        @DisplayName("候选人查看自己的投递 → 200 + phone 可见 + allowedTransitions=null")
        void ownerCandidate() {
            setAuth(99L, "CANDIDATE");
            Application app = loadedApp(500L, 1L, 99L, ApplicationStage.APPLIED);
            app.setPhone("13800000000");
            when(applicationMapper.selectById(500L)).thenReturn(app);
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            ApplicationDetailVO vo = applicationService.getDetail(500L);

            assertThat(vo.getPhone()).isEqualTo("13800000000");
            assertThat(vo.getAllowedTransitions()).isNull();
        }

        @Test
        @DisplayName("候选人查看别人的投递 → APPLICATION_ACCESS_DENIED")
        void foreignCandidate_denied() {
            setAuth(98L, "CANDIDATE"); // 非 owner（owner=99）
            when(applicationMapper.selectById(500L)).thenReturn(loadedApp(500L, 1L, 99L, ApplicationStage.APPLIED));
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> applicationService.getDetail(500L))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.APPLICATION_ACCESS_DENIED));
        }

        @Test
        @DisplayName("HR(job owner) 看 → 200 + allowedTransitions 非空")
        void hrJobOwner() {
            setAuth(10L, "HR");
            when(applicationMapper.selectById(500L)).thenReturn(loadedApp(500L, 1L, 99L, ApplicationStage.HR_INTERVIEW));
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            ApplicationDetailVO vo = applicationService.getDetail(500L);

            assertThat(vo.getStage()).isEqualTo(ApplicationStage.HR_INTERVIEW);
            assertThat(vo.getAllowedTransitions())
                    .containsExactlyInAnyOrder(ApplicationStage.OFFER, ApplicationStage.REJECTED);
        }

        @Test
        @DisplayName("投递不存在 → APPLICATION_NOT_FOUND")
        void notFound() {
            setAuth(99L, "CANDIDATE");
            when(applicationMapper.selectById(404L)).thenReturn(null);

            assertThatThrownBy(() -> applicationService.getDetail(404L))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.APPLICATION_NOT_FOUND));
        }
    }

    // ═════════════════════════════ listMine ═════════════════════════════

    @Nested
    @DisplayName("listMine · 候选人「我的投递」")
    class ListMine {

        @Test
        @DisplayName("CANDIDATE 调用 → 返回映射后的列表")
        void happyPath() {
            setAuth(99L, "CANDIDATE");
            when(applicationMapper.listByCandidate(99L)).thenReturn(List.of(
                    Map.<String, Object>ofEntries(
                            Map.entry("id", 500L),
                            Map.entry("job_id", 1L),
                            Map.entry("job_title", "Java 后端"),
                            Map.entry("job_status", "PUBLISHED"),
                            Map.entry("stage", "PHONE_INTERVIEW"),
                            Map.entry("applied_at", OffsetDateTime.now().minusDays(2)),
                            Map.entry("updated_at", OffsetDateTime.now())
                    )
            ));

            var list = applicationService.listMine();

            assertThat(list).hasSize(1);
            assertThat(list.get(0).getStage()).isEqualTo(ApplicationStage.PHONE_INTERVIEW);
            assertThat(list.get(0).getJobStatus()).isEqualTo(JobStatus.PUBLISHED);
        }

        @Test
        @DisplayName("非 CANDIDATE → FORBIDDEN")
        void nonCandidate() {
            setAuth(10L, "HR");
            assertThatThrownBy(() -> applicationService.listMine())
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));
        }
    }

    // ═════════════════════════════ board ═════════════════════════════

    @Nested
    @DisplayName("board · 看板")
    class Board {

        @Test
        @DisplayName("Admin 全局看板（jobId=null）→ 8 列固定顺序，counts 来自 mapper")
        void adminAllBoard() {
            setAuth(1L, "ADMIN");
            // 新签名：jobId=null + 无过滤 → jobIds=null（不限制） + hrUserId=null（admin）
            when(applicationMapper.countByStageFiltered(null, null)).thenReturn(List.of(
                    Map.<String, Object>of("stage", "APPLIED", "cnt", 3),
                    Map.<String, Object>of("stage", "OFFER", "cnt", 1)
            ));
            when(applicationMapper.listBoardItemsFiltered(any(), any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            BoardVO board = applicationService.board(null, 50);

            assertThat(board.getColumns()).hasSize(8);
            assertThat(board.getColumns().get(0).getStage()).isEqualTo(ApplicationStage.APPLIED);
            assertThat(board.getColumns().get(0).getCount()).isEqualTo(3);
            assertThat(board.getColumns().get(5).getStage()).isEqualTo(ApplicationStage.OFFER);
            assertThat(board.getColumns().get(5).getCount()).isEqualTo(1);
            assertThat(board.getTotalApplications()).isEqualTo(4);
        }

        @Test
        @DisplayName("HR 调他人岗位看板 → JOB_ACCESS_DENIED")
        void hrForeignJobBoard() {
            setAuth(20L, "HR");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> applicationService.board(1L, 50))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_ACCESS_DENIED));
        }

        @Test
        @DisplayName("CANDIDATE 调看板 → FORBIDDEN")
        void candidate_forbidden() {
            setAuth(99L, "CANDIDATE");
            assertThatThrownBy(() -> applicationService.board(null, 50))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));
        }
    }

    // ═════════════════════════════ helpers ═════════════════════════════

    private static void setAuth(long userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static Job loadedJob(long id, long createdBy, JobStatus status) {
        Job j = new Job();
        j.setId(id);
        j.setCreatedBy(createdBy);
        j.setSubDepartmentId(1L);
        j.setTitle("title-" + id);
        j.setWorkType(JobWorkType.FULL_TIME.name());
        j.setLevel(JobLevel.MID.name());
        j.setStatus(status.name());
        j.setViewCount(0);
        j.setCreatedAt(OffsetDateTime.now());
        j.setUpdatedAt(OffsetDateTime.now());
        return j;
    }

    private static Application loadedApp(long id, long jobId, long candidateId, ApplicationStage stage) {
        Application a = new Application();
        a.setId(id);
        a.setJobId(jobId);
        a.setCandidateId(candidateId);
        a.setStage(stage.name());
        a.setAppliedAt(OffsetDateTime.now().minusDays(1));
        a.setUpdatedAt(OffsetDateTime.now());
        return a;
    }

    private static User makeUser(long id, String name, String email, String role) {
        User u = new User();
        u.setId(id);
        u.setFullName(name);
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    /** 模拟 ApplicationMapper.insert 写回 generated id。 */
    private static void doAnswerSetAppId(ApplicationMapper mapper, long id) {
        when(mapper.insert(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(id);
            return 1;
        });
    }
}
