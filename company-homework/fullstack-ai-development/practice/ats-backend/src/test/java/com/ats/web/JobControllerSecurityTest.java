package com.ats.web;

import com.ats.auth.JwtAuthEntryPoint;
import com.ats.auth.JwtAuthenticationFilter;
import com.ats.auth.JwtService;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.exception.GlobalExceptionHandler;
import com.ats.config.SecurityConfig;
import com.ats.controller.JobController;
import com.ats.controller.TagController;
import com.ats.entity.JobStatus;
import com.ats.job.JobService;
import com.ats.job.JobService.PageResult;
import com.ats.job.TagService;
import com.ats.job.dto.JobDetailVO;
import com.ats.repository.JobMapper;
import com.ats.repository.JobTagMapper;
import com.ats.repository.RefreshTokenMapper;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Job/Tag Controller 权限矩阵 + 状态码集成测试。
 *
 * <ul>
 *   <li><b>列表 / 详情公开</b>：匿名 200，受 service 内部可见性裁剪</li>
 *   <li><b>POST/PATCH/POST transitions</b>：仅 HR/ADMIN（非 admin role 401/403）</li>
 *   <li><b>DELETE</b>：仅 ADMIN</li>
 *   <li><b>GET /tags 公开</b></li>
 *   <li><b>业务错误 → HTTP 映射</b>：JOB_NOT_FOUND→404 / JOB_NOT_PUBLISHED→403 /
 *       ILLEGAL_TRANSITION→409 / JOB_SALARY_RANGE_INVALID→409</li>
 * </ul>
 */
@WebMvcTest(controllers = {JobController.class, TagController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthEntryPoint.class,
        GlobalExceptionHandler.class,
})
@ActiveProfiles("test")
@DisplayName("Job/Tag Controller · 权限矩阵 + 错误码")
class JobControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean JobService jobService;
    @MockitoBean TagService tagService;

    // @WebMvcTest 不启用 MyBatis 自动配置，但 @MapperScan 仍会尝试创建 mapper bean → 必须全部 mock 掉
    @MockitoBean JobMapper jobMapper;
    @MockitoBean TagMapper tagMapper;
    @MockitoBean JobTagMapper jobTagMapper;
    @MockitoBean UserMapper userMapper;
    @MockitoBean RefreshTokenMapper refreshTokenMapper;
    @MockitoBean com.ats.repository.ApplicationMapper applicationMapper;
    @MockitoBean com.ats.repository.StageLogMapper stageLogMapper;
    @MockitoBean com.ats.repository.InterviewMapper interviewMapper;
    @MockitoBean com.ats.repository.StatsMapper statsMapper;
    // M6: 新增组织树相关 mapper，@MapperScan 会扫到必须 mock
    @MockitoBean com.ats.repository.RootOrgMapper rootOrgMapper;
    @MockitoBean com.ats.repository.DepartmentMapper departmentMapper;
    @MockitoBean com.ats.repository.SubDepartmentMapper subDepartmentMapper;
    @MockitoBean com.ats.repository.HrSubDepartmentMapper hrSubDepartmentMapper;
    @MockitoBean org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // ═════════════════════════════ 公开放行 ═════════════════════════════

    @Nested
    @DisplayName("公开路径 · GET /jobs · GET /jobs/{id} · GET /tags")
    class PublicEndpoints {

        @Test
        @DisplayName("匿名 GET /jobs → 200 + 调 jobService.list")
        void anonymousList_returns200() throws Exception {
            when(jobService.list(any())).thenReturn(new PageResult<>(Collections.emptyList(), 0L, 1, 20));

            mvc.perform(MockMvcRequestBuilders.get("/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("匿名 GET /jobs/1 → 200 + 调 jobService.getDetail")
        void anonymousDetail_returns200() throws Exception {
            when(jobService.getDetail(1L)).thenReturn(
                    JobDetailVO.builder().id(1L).status(JobStatus.PUBLISHED).build());

            mvc.perform(MockMvcRequestBuilders.get("/jobs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
        }

        @Test
        @DisplayName("匿名 GET /jobs/999 → service 抛 JOB_NOT_FOUND → 404")
        void detailNotFound_returns404() throws Exception {
            when(jobService.getDetail(999L))
                    .thenThrow(new BizException(ErrorCode.JOB_NOT_FOUND));

            mvc.perform(MockMvcRequestBuilders.get("/jobs/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.JOB_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("匿名 GET DRAFT 详情 → service 抛 JOB_NOT_PUBLISHED → 403")
        void detailDraftBlocked_returns403() throws Exception {
            when(jobService.getDetail(2L))
                    .thenThrow(new BizException(ErrorCode.JOB_NOT_PUBLISHED));

            mvc.perform(MockMvcRequestBuilders.get("/jobs/2"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.JOB_NOT_PUBLISHED.getCode()));
        }

        @Test
        @DisplayName("匿名 GET /tags → 200")
        void anonymousTags_returns200() throws Exception {
            when(tagService.listAll()).thenReturn(Collections.emptyList());

            mvc.perform(MockMvcRequestBuilders.get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
    }

    // ═════════════════════════════ 写操作权限 ═════════════════════════════

    @Nested
    @DisplayName("POST /jobs · 创建岗位 · HR / ADMIN only")
    class CreateAuth {

        /** M6 起 subDepartmentId 必填，否则 @Valid 在鉴权前即 400 */
        private static final String BODY = """
                {"title":"Java","description":"d","workType":"FULL_TIME","level":"MID","subDepartmentId":1}
                """;

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.post("/jobs")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isUnauthorized());

            verify(jobService, never()).create(any());
        }

        @Test
        @DisplayName("CANDIDATE → 403")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 9L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.post("/jobs")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());

            verify(jobService, never()).create(any());
        }

        @Test
        @DisplayName("HR → 200，调 service.create")
        void hr_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(jobService.create(any())).thenReturn(
                    JobDetailVO.builder().id(100L).status(JobStatus.DRAFT).build());

            mvc.perform(MockMvcRequestBuilders.post("/jobs")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(100));
        }

        @Test
        @DisplayName("ADMIN → 200")
        void admin_200() throws Exception {
            mockClaims("ad-tok", 1L, "ADMIN");
            when(jobService.create(any())).thenReturn(
                    JobDetailVO.builder().id(101L).status(JobStatus.DRAFT).build());

            mvc.perform(MockMvcRequestBuilders.post("/jobs")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY)
                            .header("Authorization", "Bearer ad-tok"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("HR + title 缺失 → 400 VALIDATION_FAILED")
        void hr_validationFailed_400() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            mvc.perform(MockMvcRequestBuilders.post("/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"workType\":\"FULL_TIME\"}")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        }
    }

    @Nested
    @DisplayName("POST /jobs/{id}/transitions · 状态机推进 · HR / ADMIN only")
    class TransitionAuth {

        private static final String BODY = "{\"to\":\"PUBLISHED\"}";

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.post("/jobs/1/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("CANDIDATE → 403")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 9L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.post("/jobs/1/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("HR · 非法流转 → service 抛 ILLEGAL_TRANSITION → 409")
        void hr_illegal_409() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(jobService.transition(anyLong(), any()))
                    .thenThrow(new BizException(ErrorCode.ILLEGAL_TRANSITION,
                            "非法状态流转：PUBLISHED → DRAFT。允许的下一步：[PAUSED, CLOSED]"));

            mvc.perform(MockMvcRequestBuilders.post("/jobs/1/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content("{\"to\":\"DRAFT\"}")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.ILLEGAL_TRANSITION.getCode()));
        }

        @Test
        @DisplayName("HR · 非 owner → service 抛 JOB_ACCESS_DENIED → 403")
        void hr_notOwner_403() throws Exception {
            mockClaims("hr-tok", 20L, "HR");
            when(jobService.transition(anyLong(), any()))
                    .thenThrow(new BizException(ErrorCode.JOB_ACCESS_DENIED));

            mvc.perform(MockMvcRequestBuilders.post("/jobs/1/transitions")
                            .contentType(MediaType.APPLICATION_JSON).content(BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.JOB_ACCESS_DENIED.getCode()));
        }
    }

    @Nested
    @DisplayName("DELETE /jobs/{id} · 软删 · ADMIN only")
    class DeleteAuth {

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.delete("/jobs/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("HR → 403")
        void hr_403() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            mvc.perform(MockMvcRequestBuilders.delete("/jobs/1")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden());

            verify(jobService, never()).softDelete(anyLong());
        }

        @Test
        @DisplayName("ADMIN → 200")
        void admin_200() throws Exception {
            mockClaims("ad-tok", 1L, "ADMIN");
            mvc.perform(MockMvcRequestBuilders.delete("/jobs/5")
                            .header("Authorization", "Bearer ad-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(jobService).softDelete(5L);
        }
    }

    // ═════════════════════════════ helpers ═════════════════════════════

    private void mockClaims(String token, long userId, String role) {
        HashMap<String, Object> raw = new HashMap<>();
        raw.put(Claims.SUBJECT, String.valueOf(userId));
        raw.put("role", role);
        Claims claims = new DefaultClaims(raw);
        when(jwtService.verifyAccessToken(token)).thenReturn(claims);
    }

    @SuppressWarnings("unused")
    private static void avoidUnusedImport() {
        List.of();
    }
}
