package com.ats.web;

import com.ats.auth.JwtAuthEntryPoint;
import com.ats.auth.JwtAuthenticationFilter;
import com.ats.auth.JwtService;
import com.ats.common.exception.GlobalExceptionHandler;
import com.ats.config.SecurityConfig;
import com.ats.controller.StatsController;
import com.ats.entity.ApplicationStage;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.InterviewMapper;
import com.ats.repository.JobMapper;
import com.ats.repository.JobTagMapper;
import com.ats.repository.RefreshTokenMapper;
import com.ats.repository.StageLogMapper;
import com.ats.repository.StatsMapper;
import com.ats.repository.TagMapper;
import com.ats.repository.UserMapper;
import com.ats.stats.StatsService;
import com.ats.stats.dto.FunnelItemVO;
import com.ats.stats.dto.FunnelVO;
import com.ats.stats.dto.OverviewVO;
import com.ats.stats.dto.PublicStatsVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StatsController 权限矩阵 · M5.1。
 *
 * <ul>
 *   <li>GET /stats/funnel · /stats/overview → HR/ADMIN（CANDIDATE 403、匿名 401）</li>
 *   <li>jobId 参数透传到 service</li>
 *   <li>response 数据结构验证</li>
 * </ul>
 */
@WebMvcTest(controllers = StatsController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthEntryPoint.class,
        GlobalExceptionHandler.class,
})
@ActiveProfiles("test")
@DisplayName("StatsController · 权限矩阵 + 数据结构")
class StatsControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean StatsService statsService;

    // 全部 mapper 都 mock 防止 @MapperScan 反向坏掉
    @MockitoBean JobMapper jobMapper;
    @MockitoBean TagMapper tagMapper;
    @MockitoBean JobTagMapper jobTagMapper;
    @MockitoBean UserMapper userMapper;
    @MockitoBean RefreshTokenMapper refreshTokenMapper;
    @MockitoBean ApplicationMapper applicationMapper;
    @MockitoBean StageLogMapper stageLogMapper;
    @MockitoBean InterviewMapper interviewMapper;
    @MockitoBean StatsMapper statsMapper;
    @MockitoBean com.ats.repository.RootOrgMapper rootOrgMapper;
    @MockitoBean com.ats.repository.DepartmentMapper departmentMapper;
    @MockitoBean com.ats.repository.SubDepartmentMapper subDepartmentMapper;
    @MockitoBean com.ats.repository.HrSubDepartmentMapper hrSubDepartmentMapper;
    @MockitoBean org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("GET /stats/funnel")
    class Funnel {

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/stats/funnel"))
                    .andExpect(status().isUnauthorized());
            verify(statsService, never()).funnel(anyLong());
            verify(statsService, never()).funnel(isNull());
        }

        @Test
        @DisplayName("CANDIDATE → 403")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.get("/stats/funnel")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("HR → 200，返回 8 态")
        void hr_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            FunnelVO funnel = FunnelVO.builder()
                    .items(List.of(
                            FunnelItemVO.builder().stage(ApplicationStage.APPLIED).count(12L).build(),
                            FunnelItemVO.builder().stage(ApplicationStage.OFFER).count(1L).build()
                    ))
                    .total(13L).max(12L).build();
            when(statsService.funnel(isNull())).thenReturn(funnel);

            mvc.perform(MockMvcRequestBuilders.get("/stats/funnel")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.total").value(13))
                    .andExpect(jsonPath("$.data.max").value(12))
                    .andExpect(jsonPath("$.data.items[0].stage").value("APPLIED"));
        }

        @Test
        @DisplayName("ADMIN + jobId → 透传")
        void admin_with_jobId() throws Exception {
            mockClaims("ad-tok", 1L, "ADMIN");
            when(statsService.funnel(42L)).thenReturn(
                    FunnelVO.builder().items(List.of()).total(0L).max(0L).build());

            mvc.perform(MockMvcRequestBuilders.get("/stats/funnel?jobId=42")
                            .header("Authorization", "Bearer ad-tok"))
                    .andExpect(status().isOk());
            verify(statsService).funnel(42L);
        }
    }

    @Nested
    @DisplayName("GET /stats/overview")
    class Overview {

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/stats/overview"))
                    .andExpect(status().isUnauthorized());
            verify(statsService, never()).overview();
        }

        @Test
        @DisplayName("CANDIDATE → 403")
        void candidate_403() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            mvc.perform(MockMvcRequestBuilders.get("/stats/overview")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isForbidden());
            verify(statsService, never()).overview();
        }

        @Test
        @DisplayName("HR → 200，返回 4 指标")
        void hr_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(statsService.overview()).thenReturn(OverviewVO.builder()
                    .newApplicationsThisMonth(20L)
                    .offersThisMonth(3L)
                    .hiresThisMonth(1L)
                    .activeJobs(7L)
                    .build());

            mvc.perform(MockMvcRequestBuilders.get("/stats/overview")
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.newApplicationsThisMonth").value(20))
                    .andExpect(jsonPath("$.data.offersThisMonth").value(3))
                    .andExpect(jsonPath("$.data.hiresThisMonth").value(1))
                    .andExpect(jsonPath("$.data.activeJobs").value(7));
        }

        @Test
        @DisplayName("ADMIN → 200")
        void admin_200() throws Exception {
            mockClaims("ad-tok", 1L, "ADMIN");
            when(statsService.overview()).thenReturn(OverviewVO.builder().build());
            mvc.perform(MockMvcRequestBuilders.get("/stats/overview")
                            .header("Authorization", "Bearer ad-tok"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /stats/public · permitAll 公开统计")
    class PublicStats {

        @Test
        @DisplayName("匿名 → 200（permitAll 放行）")
        void anonymous_200() throws Exception {
            when(statsService.publicStats()).thenReturn(PublicStatsVO.builder()
                    .screeningCount(17L)
                    .interviewCount(6L)
                    .offerCount(4L)
                    .publishedJobs(15L)
                    .coveredDepartments(4L)
                    .build());

            mvc.perform(MockMvcRequestBuilders.get("/stats/public"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.screeningCount").value(17))
                    .andExpect(jsonPath("$.data.interviewCount").value(6))
                    .andExpect(jsonPath("$.data.offerCount").value(4))
                    .andExpect(jsonPath("$.data.publishedJobs").value(15))
                    .andExpect(jsonPath("$.data.coveredDepartments").value(4));
        }

        @Test
        @DisplayName("CANDIDATE 登录 → 200（permitAll 不区分角色）")
        void candidate_200() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(statsService.publicStats()).thenReturn(PublicStatsVO.builder().build());
            mvc.perform(MockMvcRequestBuilders.get("/stats/public")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("零数据 → 200，所有字段为 0（前端会模糊化）")
        void zero_data_200() throws Exception {
            when(statsService.publicStats()).thenReturn(PublicStatsVO.builder()
                    .screeningCount(0L)
                    .interviewCount(0L)
                    .offerCount(0L)
                    .publishedJobs(0L)
                    .coveredDepartments(0L)
                    .build());

            mvc.perform(MockMvcRequestBuilders.get("/stats/public"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.screeningCount").value(0))
                    .andExpect(jsonPath("$.data.coveredDepartments").value(0));
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
