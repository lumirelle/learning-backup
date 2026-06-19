package com.ats.web;

import com.ats.auth.JwtAuthEntryPoint;
import com.ats.auth.JwtAuthenticationFilter;
import com.ats.auth.JwtService;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.exception.GlobalExceptionHandler;
import com.ats.config.SecurityConfig;
import com.ats.controller.InterviewController;
import com.ats.entity.InterviewConclusion;
import com.ats.interview.InterviewService;
import com.ats.interview.dto.InterviewVO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * InterviewController 权限矩阵 + 错误码 → HTTP 映射（M4）。
 *
 * <ul>
 *   <li>GET / POST /applications/{id}/interviews → HR/ADMIN（CANDIDATE 403、匿名 401）</li>
 *   <li>PUT /interviews/{id} → HR/ADMIN（同上）</li>
 *   <li>错误码：INTERVIEW_NOT_FOUND→404 / INTERVIEW_EDIT_EXPIRED→409 /
 *       INTERVIEW_EDIT_FORBIDDEN→403 / APPLICATION_ACCESS_DENIED→403 / VALIDATION_FAILED→400</li>
 * </ul>
 */
@WebMvcTest(controllers = InterviewController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthEntryPoint.class,
        GlobalExceptionHandler.class,
})
@ActiveProfiles("test")
@DisplayName("InterviewController · 权限矩阵 + 错误码")
class InterviewControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean InterviewService interviewService;

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

    private static final String CREATE_BODY = """
            {"round":"技术一面","rating":4,"conclusion":"PASS","strengths":"基础好"}
            """;
    private static final String UPDATE_BODY = """
            {"round":"技术一面（修订）","rating":5,"conclusion":"PASS","strengths":"追加"}
            """;

    @Nested
    @DisplayName("GET /applications/{id}/interviews")
    class List {

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/applications/500/interviews"))
                    .andExpect(status().isUnauthorized());
            verify(interviewService, never()).listByApplication(anyLong());
        }

        @Test
        @DisplayName("CANDIDATE → 403")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.get("/applications/500/interviews")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());
            verify(interviewService, never()).listByApplication(anyLong());
        }

        @Test
        @DisplayName("HR → 200")
        void hr_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(interviewService.listByApplication(500L)).thenReturn(Collections.emptyList());

            mvc.perform(MockMvcRequestBuilders.get("/applications/500/interviews")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        @Test
        @DisplayName("ADMIN → 200")
        void admin_200() throws Exception {
            mockClaims("ad-tok", 1L, "ADMIN");
            when(interviewService.listByApplication(500L)).thenReturn(Collections.emptyList());

            mvc.perform(MockMvcRequestBuilders.get("/applications/500/interviews")
                            .header("Authorization", "Bearer ad-tok"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("APPLICATION_ACCESS_DENIED → 403")
        void access_denied_403() throws Exception {
            mockClaims("hr-tok", 20L, "HR");
            when(interviewService.listByApplication(anyLong()))
                    .thenThrow(new BizException(ErrorCode.APPLICATION_ACCESS_DENIED));

            mvc.perform(MockMvcRequestBuilders.get("/applications/500/interviews")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.APPLICATION_ACCESS_DENIED.getCode()));
        }
    }

    @Nested
    @DisplayName("POST /applications/{id}/interviews")
    class Create {

        @Test
        @DisplayName("CANDIDATE → 403")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.post("/applications/500/interviews")
                            .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("HR 合法 → 200")
        void hr_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(interviewService.add(eq(500L), any())).thenReturn(
                    InterviewVO.builder().id(9001L).applicationId(500L)
                            .conclusion(InterviewConclusion.PASS).editable(true).build());

            mvc.perform(MockMvcRequestBuilders.post("/applications/500/interviews")
                            .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(9001))
                    .andExpect(jsonPath("$.data.editable").value(true));
        }

        @Test
        @DisplayName("缺 round → 400 VALIDATION_FAILED")
        void missing_round_400() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            mvc.perform(MockMvcRequestBuilders.post("/applications/500/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {"rating":4,"conclusion":"PASS"}
                                """)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        }

        @Test
        @DisplayName("rating 越界 → 400")
        void rating_out_of_range_400() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            mvc.perform(MockMvcRequestBuilders.post("/applications/500/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {"round":"x","rating":7,"conclusion":"PASS"}
                                """)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("非 owner HR → APPLICATION_ACCESS_DENIED → 403")
        void non_owner_403() throws Exception {
            mockClaims("hr-tok", 20L, "HR");
            when(interviewService.add(anyLong(), any()))
                    .thenThrow(new BizException(ErrorCode.APPLICATION_ACCESS_DENIED));

            mvc.perform(MockMvcRequestBuilders.post("/applications/500/interviews")
                            .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /interviews/{id}")
    class Update {

        @Test
        @DisplayName("HR 自己写的 1h 内 → 200")
        void hr_within_window_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(interviewService.update(eq(9001L), any())).thenReturn(
                    InterviewVO.builder().id(9001L).editable(true).build());

            mvc.perform(MockMvcRequestBuilders.put("/interviews/9001")
                            .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("INTERVIEW_EDIT_EXPIRED → 409")
        void expired_409() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(interviewService.update(anyLong(), any()))
                    .thenThrow(new BizException(ErrorCode.INTERVIEW_EDIT_EXPIRED));

            mvc.perform(MockMvcRequestBuilders.put("/interviews/9001")
                            .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INTERVIEW_EDIT_EXPIRED.getCode()));
        }

        @Test
        @DisplayName("INTERVIEW_EDIT_FORBIDDEN → 403")
        void forbidden_403() throws Exception {
            mockClaims("hr-tok", 11L, "HR");
            when(interviewService.update(anyLong(), any()))
                    .thenThrow(new BizException(ErrorCode.INTERVIEW_EDIT_FORBIDDEN));

            mvc.perform(MockMvcRequestBuilders.put("/interviews/9001")
                            .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INTERVIEW_EDIT_FORBIDDEN.getCode()));
        }

        @Test
        @DisplayName("INTERVIEW_NOT_FOUND → 404")
        void not_found_404() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(interviewService.update(anyLong(), any()))
                    .thenThrow(new BizException(ErrorCode.INTERVIEW_NOT_FOUND));

            mvc.perform(MockMvcRequestBuilders.put("/interviews/404")
                            .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isNotFound());
        }
    }

    private void mockClaims(String token, long userId, String role) {
        HashMap<String, Object> raw = new HashMap<>();
        raw.put(Claims.SUBJECT, String.valueOf(userId));
        raw.put("email", "u" + userId + "@b.com");
        raw.put("role", role);
        when(jwtService.verifyAccessToken(token)).thenReturn(new DefaultClaims(raw));
    }
}
