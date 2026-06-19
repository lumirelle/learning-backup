package com.ats.web;

import com.ats.application.ApplicationService;
import com.ats.application.dto.ApplicationDetailVO;
import com.ats.application.dto.BoardQueryReq;
import com.ats.application.dto.BoardVO;
import com.ats.auth.JwtAuthEntryPoint;
import com.ats.auth.JwtAuthenticationFilter;
import com.ats.auth.JwtService;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.exception.GlobalExceptionHandler;
import com.ats.config.SecurityConfig;
import com.ats.controller.ApplicationController;
import com.ats.entity.ApplicationStage;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.InterviewMapper;
import com.ats.repository.JobMapper;
import com.ats.repository.JobTagMapper;
import com.ats.repository.RefreshTokenMapper;
import com.ats.repository.StageLogMapper;
import com.ats.repository.TagMapper;
import com.ats.repository.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Application Controller 权限矩阵 + 错误码集成测试（M3）。
 *
 * <ul>
 *   <li><b>POST /applications</b>：CANDIDATE only（401 / 403 / 200）</li>
 *   <li><b>GET /applications/me</b>：CANDIDATE only</li>
 *   <li><b>GET /applications/board</b>：HR / ADMIN only</li>
 *   <li><b>GET /applications/{id}</b>：要登录（401），service 内部裁剪权限</li>
 *   <li><b>POST /applications/{id}/transitions</b>：HR / ADMIN only</li>
 *   <li><b>错误码 → HTTP</b>：ILLEGAL_TRANSITION→409 / APPLICATION_TERMINATED→409 /
 *       APPLICATION_ACCESS_DENIED→403 / APPLICATION_NOT_FOUND→404 / REJECT_REASON_REQUIRED→409 /
 *       JOB_NOT_HIRING→409 / SELF_APPLY_FORBIDDEN→403</li>
 * </ul>
 */
@WebMvcTest(controllers = ApplicationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthEntryPoint.class,
        GlobalExceptionHandler.class,
})
@ActiveProfiles("test")
@DisplayName("ApplicationController · 权限矩阵 + 错误码")
class ApplicationControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean ApplicationService applicationService;

    // 全部 mapper（@MapperScan 扫到的）+ PasswordEncoder
    @MockitoBean JobMapper jobMapper;
    @MockitoBean TagMapper tagMapper;
    @MockitoBean JobTagMapper jobTagMapper;
    @MockitoBean UserMapper userMapper;
    @MockitoBean RefreshTokenMapper refreshTokenMapper;
    @MockitoBean ApplicationMapper applicationMapper;
    @MockitoBean StageLogMapper stageLogMapper;
    @MockitoBean InterviewMapper interviewMapper;
    @MockitoBean com.ats.repository.StatsMapper statsMapper;
    @MockitoBean com.ats.repository.RootOrgMapper rootOrgMapper;
    @MockitoBean com.ats.repository.DepartmentMapper departmentMapper;
    @MockitoBean com.ats.repository.SubDepartmentMapper subDepartmentMapper;
    @MockitoBean com.ats.repository.HrSubDepartmentMapper hrSubDepartmentMapper;
    @MockitoBean org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final String APPLY_BODY = """
            {"jobId":1,"yearsExp":3,"phone":"13800000000"}
            """;
    private static final String TRANSITION_BODY = """
            {"toStage":"SCREENING_PASS"}
            """;
    private static final String REJECT_BODY = """
            {"toStage":"REJECTED"}
            """;
    private static final String REJECT_BODY_WITH_NOTE = """
            {"toStage":"REJECTED","note":"技术不达标"}
            """;

    // ═════════════════════════════ POST /applications ═════════════════════════════

    @Nested
    @DisplayName("POST /applications · 投递")
    class Apply {

        @Test
        @DisplayName("匿名 → 401，不调 service")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.post("/applications")
                            .contentType(MediaType.APPLICATION_JSON).content(APPLY_BODY))
                    .andExpect(status().isUnauthorized());
            verify(applicationService, never()).apply(any());
        }

        @Test
        @DisplayName("HR → 403（只允许 CANDIDATE）")
        void hr_403() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            mvc.perform(MockMvcRequestBuilders.post("/applications")
                            .contentType(MediaType.APPLICATION_JSON).content(APPLY_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden());
            verify(applicationService, never()).apply(any());
        }

        @Test
        @DisplayName("CANDIDATE → 200，透传 service.apply")
        void candidate_200() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(applicationService.apply(any())).thenReturn(
                    ApplicationDetailVO.builder().id(500L).stage(ApplicationStage.APPLIED).build());

            mvc.perform(MockMvcRequestBuilders.post("/applications")
                            .contentType(MediaType.APPLICATION_JSON).content(APPLY_BODY)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(500))
                    .andExpect(jsonPath("$.data.stage").value("APPLIED"));
        }

        @Test
        @DisplayName("缺 jobId → 400 VALIDATION_FAILED")
        void missingJobId_400() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.post("/applications")
                            .contentType(MediaType.APPLICATION_JSON).content("{}")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        }

        @Test
        @DisplayName("service 抛 JOB_NOT_HIRING → 409 + 业务码")
        void notHiring_409() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(applicationService.apply(any()))
                    .thenThrow(new BizException(ErrorCode.JOB_NOT_HIRING));

            mvc.perform(MockMvcRequestBuilders.post("/applications")
                            .contentType(MediaType.APPLICATION_JSON).content(APPLY_BODY)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.JOB_NOT_HIRING.getCode()));
        }

        @Test
        @DisplayName("service 抛 SELF_APPLY_FORBIDDEN → 403")
        void selfApply_403() throws Exception {
            mockClaims("c-tok", 10L, "CANDIDATE");
            when(applicationService.apply(any()))
                    .thenThrow(new BizException(ErrorCode.SELF_APPLY_FORBIDDEN));

            mvc.perform(MockMvcRequestBuilders.post("/applications")
                            .contentType(MediaType.APPLICATION_JSON).content(APPLY_BODY)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.SELF_APPLY_FORBIDDEN.getCode()));
        }
    }

    // ═════════════════════════════ GET /applications/me ═════════════════════════════

    @Nested
    @DisplayName("GET /applications/me · 我的投递")
    class ListMine {

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/applications/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("HR → 403")
        void hr_403() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            mvc.perform(MockMvcRequestBuilders.get("/applications/me")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CANDIDATE → 200")
        void candidate_200() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(applicationService.listMine()).thenReturn(Collections.emptyList());

            mvc.perform(MockMvcRequestBuilders.get("/applications/me")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
    }

    // ═════════════════════════════ GET /applications/board ═════════════════════════════

    @Nested
    @DisplayName("GET /applications/board · 看板")
    class Board {

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/applications/board"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("CANDIDATE → 403")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.get("/applications/board")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("HR 全局看板 → 200")
        void hrAll_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            // controller 改用 @ModelAttribute BoardQueryReq，jobId 为空时 BoardQueryReq.jobId == null
            when(applicationService.board(argThat((BoardQueryReq r) -> r != null && r.getJobId() == null)))
                    .thenReturn(BoardVO.builder().columns(Collections.emptyList()).totalApplications(0).build());

            mvc.perform(MockMvcRequestBuilders.get("/applications/board")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("HR 调他人岗位看板 → service 抛 JOB_ACCESS_DENIED → 403")
        void hrForeignJob_403() throws Exception {
            mockClaims("hr-tok", 20L, "HR");
            when(applicationService.board(argThat((BoardQueryReq r) -> r != null && Long.valueOf(1L).equals(r.getJobId()))))
                    .thenThrow(new BizException(ErrorCode.JOB_ACCESS_DENIED));

            mvc.perform(MockMvcRequestBuilders.get("/applications/board?jobId=1")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.JOB_ACCESS_DENIED.getCode()));
        }
    }

    // ═════════════════════════════ GET /applications/{id} ═════════════════════════════

    @Nested
    @DisplayName("GET /applications/{id} · 详情")
    class Detail {

        @Test
        @DisplayName("匿名 → 401（接口要求登录）")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/applications/500"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("CANDIDATE 看自己的投递 → 200")
        void candidateOwn_200() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(applicationService.getDetail(500L)).thenReturn(
                    ApplicationDetailVO.builder().id(500L).candidateId(99L).build());

            mvc.perform(MockMvcRequestBuilders.get("/applications/500")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(500));
        }

        @Test
        @DisplayName("APPLICATION_NOT_FOUND → 404")
        void notFound_404() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(applicationService.getDetail(404L))
                    .thenThrow(new BizException(ErrorCode.APPLICATION_NOT_FOUND));

            mvc.perform(MockMvcRequestBuilders.get("/applications/404")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.APPLICATION_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("APPLICATION_ACCESS_DENIED → 403")
        void accessDenied_403() throws Exception {
            mockClaims("c-tok", 98L, "CANDIDATE");
            when(applicationService.getDetail(500L))
                    .thenThrow(new BizException(ErrorCode.APPLICATION_ACCESS_DENIED));

            mvc.perform(MockMvcRequestBuilders.get("/applications/500")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.APPLICATION_ACCESS_DENIED.getCode()));
        }
    }

    // ═════════════════════════════ POST /applications/{id}/transitions ═════════════════════════════

    @Nested
    @DisplayName("POST /applications/{id}/transitions · 阶段流转")
    class Transitions {

        @Test
        @DisplayName("CANDIDATE → 403（只允许 HR / ADMIN）")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.post("/applications/500/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(TRANSITION_BODY)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());
            verify(applicationService, never()).transition(anyLong(), any(), any());
        }

        @Test
        @DisplayName("ILLEGAL_TRANSITION → 409")
        void illegal_409() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(applicationService.transition(anyLong(), any(), any()))
                    .thenThrow(new BizException(ErrorCode.ILLEGAL_TRANSITION, "APPLIED → OFFER 非法"));

            mvc.perform(MockMvcRequestBuilders.post("/applications/500/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(TRANSITION_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.ILLEGAL_TRANSITION.getCode()));
        }

        @Test
        @DisplayName("APPLICATION_TERMINATED → 409")
        void terminated_409() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(applicationService.transition(anyLong(), any(), any()))
                    .thenThrow(new BizException(ErrorCode.APPLICATION_TERMINATED));

            mvc.perform(MockMvcRequestBuilders.post("/applications/500/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(TRANSITION_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.APPLICATION_TERMINATED.getCode()));
        }

        @Test
        @DisplayName("REJECTED 缺 note → REJECT_REASON_REQUIRED → 409")
        void rejectMissingNote_409() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(applicationService.transition(anyLong(), any(), any()))
                    .thenThrow(new BizException(ErrorCode.REJECT_REASON_REQUIRED));

            mvc.perform(MockMvcRequestBuilders.post("/applications/500/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(REJECT_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.REJECT_REASON_REQUIRED.getCode()));
        }

        @Test
        @DisplayName("HR 合法流转 → 200")
        void hrLegal_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(applicationService.transition(eq(500L), eq(ApplicationStage.REJECTED), eq("技术不达标")))
                    .thenReturn(ApplicationDetailVO.builder()
                            .id(500L).stage(ApplicationStage.REJECTED).rejectReason("技术不达标").build());

            mvc.perform(MockMvcRequestBuilders.post("/applications/500/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(REJECT_BODY_WITH_NOTE)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stage").value("REJECTED"))
                    .andExpect(jsonPath("$.data.rejectReason").value("技术不达标"));
        }

        @Test
        @DisplayName("非 owner HR → APPLICATION_ACCESS_DENIED → 403")
        void nonOwnerHr_403() throws Exception {
            mockClaims("hr-tok", 20L, "HR");
            when(applicationService.transition(anyLong(), any(), any()))
                    .thenThrow(new BizException(ErrorCode.APPLICATION_ACCESS_DENIED));

            mvc.perform(MockMvcRequestBuilders.post("/applications/500/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(TRANSITION_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.APPLICATION_ACCESS_DENIED.getCode()));
        }
    }

    // ═════════════════════════════ helpers ═════════════════════════════

    private void mockClaims(String token, long userId, String role) {
        HashMap<String, Object> raw = new HashMap<>();
        raw.put(Claims.SUBJECT, String.valueOf(userId));
        raw.put("email", "u" + userId + "@b.com");
        raw.put("role", role);
        when(jwtService.verifyAccessToken(token)).thenReturn(new DefaultClaims(raw));
    }
}
