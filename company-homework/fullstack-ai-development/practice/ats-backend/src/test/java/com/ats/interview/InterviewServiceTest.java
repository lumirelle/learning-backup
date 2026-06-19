package com.ats.interview;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.Application;
import com.ats.entity.ApplicationStage;
import com.ats.entity.InterviewConclusion;
import com.ats.entity.InterviewRecord;
import com.ats.entity.Job;
import com.ats.entity.JobStatus;
import com.ats.interview.dto.InterviewCreateReq;
import com.ats.interview.dto.InterviewUpdateReq;
import com.ats.interview.dto.InterviewVO;
import com.ats.job.HrJobScopeService;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.InterviewMapper;
import com.ats.repository.JobMapper;
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

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * InterviewService 纯 Mockito 单测，覆盖：
 *  · add       ：happy / 应用不存在 / job 不存在 / 非 owner HR
 *  · update    ：作者 24h 内 OK / 25h 后 EDIT_EXPIRED / 非作者 EDIT_FORBIDDEN /
 *                ADMIN 不限时 / NOT_FOUND / 跨岗位 HR 串改
 *  · list      ：透传 mapper + editable 字段计算
 *  · helpers   ：isOutsideEditWindow / toVO（含 PG Timestamp 真实路径）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("InterviewService · 业务分支覆盖")
class InterviewServiceTest {

    @Mock InterviewMapper interviewMapper;
    @Mock ApplicationMapper applicationMapper;
    @Mock JobMapper jobMapper;
    @Mock HrJobScopeService hrJobScopeService;

    @InjectMocks InterviewService service;

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
            return java.util.Objects.equals(Long.parseLong(auth.getName()), job.getCreatedBy());
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ═════════════════════════════ add ═════════════════════════════

    @Nested
    @DisplayName("add · 添加面试评价")
    class Add {

        @Test
        @DisplayName("happy: HR(owner) → insert + 返回 VO（editable=true）")
        void happy_ownerHr() {
            setAuth(10L, "HR");
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);

            // mock insert：插入后 selectById 用于 reload
            doAnswerSetIvId(interviewMapper, 9001L);
            InterviewRecord saved = makeRecord(9001L, 500L, 10L, OffsetDateTime.now());
            when(interviewMapper.selectById(9001L)).thenReturn(saved);
            when(interviewMapper.listByApplication(500L)).thenReturn(List.of(rowOf(saved, "HR Helen", "HR")));

            InterviewCreateReq req = new InterviewCreateReq();
            req.setRound("技术一面");
            req.setRating((short) 4);
            req.setConclusion(InterviewConclusion.PASS);
            req.setStrengths("基础扎实");
            req.setWeaknesses("分布式经验少");

            InterviewVO vo = service.add(500L, req);

            assertThat(vo.getId()).isEqualTo(9001L);
            assertThat(vo.getEditable()).isTrue();
            ArgumentCaptor<InterviewRecord> cap = ArgumentCaptor.forClass(InterviewRecord.class);
            verify(interviewMapper).insert(any(InterviewRecord.class));
            // 用 captor 拿到 insert 调用时的 entity 内容
            verify(interviewMapper, org.mockito.Mockito.atLeastOnce()).insert(cap.capture());
            assertThat(cap.getValue().getInterviewerId()).isEqualTo(10L);
            assertThat(cap.getValue().getApplicationId()).isEqualTo(500L);
            assertThat(cap.getValue().getConclusion()).isEqualTo("PASS");
        }

        @Test
        @DisplayName("ADMIN → 200，interviewer = 自己")
        void admin_ok() {
            setAuth(1L, "ADMIN");
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L); // owner 是别人但 ADMIN 不限

            doAnswerSetIvId(interviewMapper, 9001L);
            InterviewRecord saved = makeRecord(9001L, 500L, 1L, OffsetDateTime.now());
            when(interviewMapper.selectById(9001L)).thenReturn(saved);
            when(interviewMapper.listByApplication(500L)).thenReturn(List.of(rowOf(saved, "Admin", "ADMIN")));

            InterviewCreateReq req = baseCreateReq();
            InterviewVO vo = service.add(500L, req);

            assertThat(vo.getId()).isEqualTo(9001L);
        }

        @Test
        @DisplayName("application 不存在 → APPLICATION_NOT_FOUND")
        void appNotFound_404() {
            setAuth(10L, "HR");
            when(applicationMapper.selectById(404L)).thenReturn(null);

            assertThatThrownBy(() -> service.add(404L, baseCreateReq()))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_NOT_FOUND);
            verify(interviewMapper, never()).insert(any(InterviewRecord.class));
        }

        @Test
        @DisplayName("非 owner HR → APPLICATION_ACCESS_DENIED")
        void nonOwnerHr_403() {
            setAuth(20L, "HR"); // owner 是 10
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);

            assertThatThrownBy(() -> service.add(500L, baseCreateReq()))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_ACCESS_DENIED);
            verify(interviewMapper, never()).insert(any(InterviewRecord.class));
        }
    }

    // ═════════════════════════════ update ═════════════════════════════

    @Nested
    @DisplayName("update · 编辑面试评价")
    class Update {

        @Test
        @DisplayName("自己写的 1h 前 → OK")
        void author_within_window() {
            setAuth(10L, "HR");
            InterviewRecord existing = makeRecord(9001L, 500L, 10L, OffsetDateTime.now().minusHours(1));
            when(interviewMapper.selectById(9001L)).thenReturn(existing);
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);
            when(interviewMapper.listByApplication(500L)).thenReturn(List.of(rowOf(existing, "HR Helen", "HR")));

            InterviewVO vo = service.update(9001L, baseUpdateReq());
            assertThat(vo.getId()).isEqualTo(9001L);
            verify(interviewMapper).updateById(any(InterviewRecord.class));
        }

        @Test
        @DisplayName("自己写的 25h 前 → INTERVIEW_EDIT_EXPIRED")
        void author_outside_window() {
            setAuth(10L, "HR");
            InterviewRecord existing = makeRecord(9001L, 500L, 10L, OffsetDateTime.now().minusHours(25));
            when(interviewMapper.selectById(9001L)).thenReturn(existing);
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);

            assertThatThrownBy(() -> service.update(9001L, baseUpdateReq()))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERVIEW_EDIT_EXPIRED);
            verify(interviewMapper, never()).updateById(any(InterviewRecord.class));
        }

        @Test
        @DisplayName("非作者 HR(同 owner) → INTERVIEW_EDIT_FORBIDDEN（不是访问被拒，是作者保护）")
        void nonAuthor_forbidden() {
            setAuth(11L, "HR"); // 与 owner 一致，但 interviewer 是 10
            InterviewRecord existing = makeRecord(9001L, 500L, 10L, OffsetDateTime.now().minusMinutes(5));
            when(interviewMapper.selectById(9001L)).thenReturn(existing);
            mockApp(500L, 1L);
            mockJobOwner(1L, 11L); // owner 是 11，非作者

            assertThatThrownBy(() -> service.update(9001L, baseUpdateReq()))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERVIEW_EDIT_FORBIDDEN);
        }

        @Test
        @DisplayName("ADMIN 改别人 99h 前的 → OK（不受时间限制）")
        void admin_unrestricted() {
            setAuth(1L, "ADMIN");
            InterviewRecord existing = makeRecord(9001L, 500L, 10L, OffsetDateTime.now().minusHours(99));
            when(interviewMapper.selectById(9001L)).thenReturn(existing);
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);
            when(interviewMapper.listByApplication(500L)).thenReturn(List.of(rowOf(existing, "HR Helen", "HR")));

            InterviewVO vo = service.update(9001L, baseUpdateReq());
            assertThat(vo.getId()).isEqualTo(9001L);
        }

        @Test
        @DisplayName("跨岗位 HR 改 → APPLICATION_ACCESS_DENIED（权限边界先于编辑窗口）")
        void cross_job_hr() {
            setAuth(20L, "HR"); // 既不是作者也不是 owner
            InterviewRecord existing = makeRecord(9001L, 500L, 10L, OffsetDateTime.now().minusMinutes(5));
            when(interviewMapper.selectById(9001L)).thenReturn(existing);
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);

            assertThatThrownBy(() -> service.update(9001L, baseUpdateReq()))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_ACCESS_DENIED);
        }

        @Test
        @DisplayName("不存在 → INTERVIEW_NOT_FOUND")
        void not_found() {
            setAuth(10L, "HR");
            when(interviewMapper.selectById(404L)).thenReturn(null);

            assertThatThrownBy(() -> service.update(404L, baseUpdateReq()))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERVIEW_NOT_FOUND);
        }
    }

    // ═════════════════════════════ list ═════════════════════════════

    @Nested
    @DisplayName("list · 列表")
    class ListByApplication {

        @Test
        @DisplayName("HR(owner) 看自己岗位 → 透传 mapper")
        void hr_owner() {
            setAuth(10L, "HR");
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);
            InterviewRecord r1 = makeRecord(1L, 500L, 10L, OffsetDateTime.now());
            InterviewRecord r2 = makeRecord(2L, 500L, 11L, OffsetDateTime.now().minusHours(48));
            when(interviewMapper.listByApplication(500L)).thenReturn(List.of(
                    rowOf(r1, "HR Helen", "HR"),
                    rowOf(r2, "HR Bob", "HR")
            ));

            List<InterviewVO> list = service.listByApplication(500L);
            assertThat(list).hasSize(2);
            // r1 自己写的 + 1 小时内 → editable=true
            assertThat(list.get(0).getEditable()).isTrue();
            // r2 别人写的 → editable=false
            assertThat(list.get(1).getEditable()).isFalse();
        }

        @Test
        @DisplayName("非 owner HR → APPLICATION_ACCESS_DENIED")
        void non_owner_403() {
            setAuth(20L, "HR");
            mockApp(500L, 1L);
            mockJobOwner(1L, 10L);

            assertThatThrownBy(() -> service.listByApplication(500L))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_ACCESS_DENIED);
        }
    }

    // ═════════════════════════════ helpers (统一测试) ═════════════════════════════

    @Nested
    @DisplayName("helpers · isOutsideEditWindow / toVO")
    class Helpers {

        @Test
        @DisplayName("isOutsideEditWindow：null=false / 0h=false / 23.9h=false / 24h=true / 100h=true")
        void edit_window_boundaries() {
            assertThat(InterviewService.isOutsideEditWindow(null)).isFalse();
            assertThat(InterviewService.isOutsideEditWindow(OffsetDateTime.now())).isFalse();
            assertThat(InterviewService.isOutsideEditWindow(OffsetDateTime.now().minusHours(23))).isFalse();
            assertThat(InterviewService.isOutsideEditWindow(OffsetDateTime.now().minusHours(24))).isTrue();
            assertThat(InterviewService.isOutsideEditWindow(OffsetDateTime.now().minusHours(100))).isTrue();
        }

        @Test
        @DisplayName("toVO 兼容 java.sql.Timestamp（M3 已沉淀的 PG timestamptz Map 路径）")
        void to_vo_with_jdbc_timestamp() {
            // 模拟真实 JDBC Map 返回：timestamptz → java.sql.Timestamp
            Timestamp ts = Timestamp.valueOf("2026-05-25 22:00:00");
            Map<String, Object> row = Map.of(
                    "id", 9001L,
                    "application_id", 500L,
                    "interviewer_id", 10L,
                    "interviewer_name", "HR Helen",
                    "interviewer_role", "HR",
                    "round", "技术一面",
                    "rating", (short) 4,
                    "conclusion", "PASS",
                    "created_at", ts,
                    "updated_at", ts
            );

            InterviewVO vo = InterviewService.toVO(row, 10L, false);
            assertThat(vo.getId()).isEqualTo(9001L);
            assertThat(vo.getInterviewerId()).isEqualTo(10L);
            assertThat(vo.getCreatedAt()).isNotNull();
            assertThat(vo.getConclusion()).isEqualTo(InterviewConclusion.PASS);
        }

        @Test
        @DisplayName("toVO editable 矩阵：自己 + 1h → true / 别人 → false / ADMIN 永远 true")
        void editable_matrix() {
            Timestamp recent = Timestamp.valueOf(OffsetDateTime.now().minusMinutes(10).toLocalDateTime());
            Map<String, Object> row = Map.of(
                    "id", 1L,
                    "application_id", 500L,
                    "interviewer_id", 10L,
                    "interviewer_name", "HR Helen",
                    "interviewer_role", "HR",
                    "round", "tech",
                    "rating", (short) 4,
                    "conclusion", "PASS",
                    "created_at", recent,
                    "updated_at", recent
            );
            assertThat(InterviewService.toVO(row, 10L, false).getEditable()).isTrue();   // self in window
            assertThat(InterviewService.toVO(row, 11L, false).getEditable()).isFalse();  // not author
            assertThat(InterviewService.toVO(row, 11L, true).getEditable()).isTrue();    // ADMIN bypass
        }
    }

    // ═════════════════════════════ helpers ═════════════════════════════

    private void setAuth(long userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void mockApp(long appId, long jobId) {
        Application app = new Application();
        app.setId(appId);
        app.setJobId(jobId);
        app.setStage(ApplicationStage.PHONE_INTERVIEW.name());
        when(applicationMapper.selectById(appId)).thenReturn(app);
    }

    private void mockJobOwner(long jobId, long createdBy) {
        Job job = new Job();
        job.setId(jobId);
        job.setCreatedBy(createdBy);
        job.setStatus(JobStatus.PUBLISHED.name());
        when(jobMapper.selectById(jobId)).thenReturn(job);
    }

    private static InterviewRecord makeRecord(long id, long appId, long interviewerId, OffsetDateTime createdAt) {
        InterviewRecord r = new InterviewRecord();
        r.setId(id);
        r.setApplicationId(appId);
        r.setInterviewerId(interviewerId);
        r.setRound("技术一面");
        r.setRating((short) 4);
        r.setConclusion("PASS");
        r.setStrengths("good");
        r.setCreatedAt(createdAt);
        r.setUpdatedAt(createdAt);
        return r;
    }

    private static Map<String, Object> rowOf(InterviewRecord r, String name, String role) {
        java.util.HashMap<String, Object> m = new java.util.HashMap<>();
        m.put("id", r.getId());
        m.put("application_id", r.getApplicationId());
        m.put("interviewer_id", r.getInterviewerId());
        m.put("interviewer_name", name);
        m.put("interviewer_role", role);
        m.put("round", r.getRound());
        m.put("rating", r.getRating());
        m.put("conclusion", r.getConclusion());
        m.put("strengths", r.getStrengths());
        m.put("weaknesses", r.getWeaknesses());
        m.put("notes", r.getNotes());
        m.put("created_at", r.getCreatedAt());
        m.put("updated_at", r.getUpdatedAt());
        return m;
    }

    private static void doAnswerSetIvId(InterviewMapper m, long generatedId) {
        when(m.insert(any(InterviewRecord.class))).thenAnswer(inv -> {
            InterviewRecord r = inv.getArgument(0);
            r.setId(generatedId);
            return 1;
        });
    }

    private static InterviewCreateReq baseCreateReq() {
        InterviewCreateReq req = new InterviewCreateReq();
        req.setRound("技术一面");
        req.setRating((short) 4);
        req.setConclusion(InterviewConclusion.PASS);
        req.setStrengths("OK");
        return req;
    }

    private static InterviewUpdateReq baseUpdateReq() {
        InterviewUpdateReq req = new InterviewUpdateReq();
        req.setRound("技术一面（修订）");
        req.setRating((short) 5);
        req.setConclusion(InterviewConclusion.PASS);
        req.setStrengths("追加：算法很强");
        return req;
    }
}
