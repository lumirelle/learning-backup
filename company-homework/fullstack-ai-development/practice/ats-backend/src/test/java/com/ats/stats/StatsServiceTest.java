package com.ats.stats;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.ApplicationStage;
import com.ats.job.HrJobScope;
import com.ats.job.HrJobScopeService;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.StatsMapper;
import com.ats.stats.dto.FunnelVO;
import com.ats.stats.dto.OverviewVO;
import com.ats.stats.dto.PublicStatsVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * StatsService 单测 · M5.1。
 *
 * <p>覆盖：</p>
 * <ul>
 *   <li>HR 视角传 hrUserId、ADMIN 传 null（与 ApplicationService.board 同构）</li>
 *   <li>funnel 8 态固定顺序（缺失 stage 补 0）+ total / max 计算正确</li>
 *   <li>overview 4 个指标 SQL 调用顺序与参数</li>
 *   <li>CANDIDATE 走到 service 直接 FORBIDDEN（虽然 controller 层有 PreAuthorize 兜底）</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("StatsService 单测")
class StatsServiceTest {

    @Mock ApplicationMapper applicationMapper;
    @Mock StatsMapper statsMapper;
    @Mock HrJobScopeService hrJobScopeService;
    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks StatsService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static HrJobScope hrScope(long userId) {
        return new HrJobScope(userId, List.of());
    }

    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(null);
        org.mockito.Mockito.lenient().doNothing().when(valueOps).set(any(), any(), any(java.time.Duration.class));
        // 注入真实 ObjectMapper 供 cache 序列化（publicStats 测试走 DB 路径）
        org.springframework.test.util.ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        when(hrJobScopeService.currentScopeOrNull()).thenAnswer(inv -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return null;
            boolean isHr = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
            return isHr ? hrScope(Long.parseLong(auth.getName())) : null;
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ═════════════════════════════ funnel ═════════════════════════════

    @Nested
    @DisplayName("funnel · 招聘漏斗")
    class Funnel {

        @Test
        @DisplayName("HR · 8 态固定顺序，缺失 stage 补 0")
        void hr_8_stages_with_zero_padding() {
            setAuth(10L, "HR");
            // 只返回 3 个 stage，其他 5 个应自动补 0
            when(applicationMapper.countByStage(isNull(), eq(hrScope(10L)))).thenReturn(List.of(
                    Map.of("stage", "APPLIED", "cnt", 12L),
                    Map.of("stage", "PHONE_INTERVIEW", "cnt", 4L),
                    Map.of("stage", "OFFER", "cnt", 1L)
            ));

            FunnelVO vo = service.funnel(null);

            assertThat(vo.getItems()).hasSize(8);
            assertThat(vo.getItems().get(0).getStage()).isEqualTo(ApplicationStage.APPLIED);
            assertThat(vo.getItems().get(0).getCount()).isEqualTo(12L);
            assertThat(vo.getItems().get(1).getStage()).isEqualTo(ApplicationStage.SCREENING_PASS);
            assertThat(vo.getItems().get(1).getCount()).isZero();
            assertThat(vo.getItems().get(2).getStage()).isEqualTo(ApplicationStage.PHONE_INTERVIEW);
            assertThat(vo.getItems().get(2).getCount()).isEqualTo(4L);
            assertThat(vo.getItems().get(7).getStage()).isEqualTo(ApplicationStage.REJECTED);
            assertThat(vo.getItems().get(7).getCount()).isZero();

            assertThat(vo.getTotal()).isEqualTo(17L);
            assertThat(vo.getMax()).isEqualTo(12L);
        }

        @Test
        @DisplayName("ADMIN · hrUserId 传 null（不裁剪）")
        void admin_passes_null_owner() {
            setAuth(1L, "ADMIN");
            when(applicationMapper.countByStage(isNull(), isNull())).thenReturn(List.of());

            FunnelVO vo = service.funnel(null);

            assertThat(vo.getTotal()).isZero();
            assertThat(vo.getMax()).isZero();
            assertThat(vo.getItems()).hasSize(8).allMatch(i -> i.getCount() == 0);
            verify(applicationMapper).countByStage(isNull(), isNull());
        }

        @Test
        @DisplayName("HR · 限定单岗位 jobId")
        void hr_with_jobId_filter() {
            setAuth(10L, "HR");
            when(applicationMapper.countByStage(eq(42L), eq(hrScope(10L)))).thenReturn(List.of(
                    Map.of("stage", "HIRED", "cnt", 1L)
            ));

            FunnelVO vo = service.funnel(42L);

            assertThat(vo.getTotal()).isEqualTo(1L);
            verify(applicationMapper).countByStage(eq(42L), eq(hrScope(10L)));
        }

        @Test
        @DisplayName("CANDIDATE · 直接 FORBIDDEN")
        void candidate_forbidden() {
            setAuth(99L, "CANDIDATE");
            assertThatThrownBy(() -> service.funnel(null))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("Number 子类型（Integer/Long/BigInteger）兼容")
        void number_subtypes_compatible() {
            setAuth(10L, "HR");
            when(applicationMapper.countByStage(isNull(), eq(hrScope(10L)))).thenReturn(List.of(
                    Map.of("stage", "APPLIED", "cnt", 5),                                   // int
                    Map.of("stage", "OFFER", "cnt", java.math.BigInteger.valueOf(3))        // BigInteger
            ));

            FunnelVO vo = service.funnel(null);
            assertThat(vo.getTotal()).isEqualTo(8L);
        }
    }

    // ═════════════════════════════ overview ═════════════════════════════

    @Nested
    @DisplayName("overview · 4 指标")
    class Overview {

        @Test
        @DisplayName("HR · 4 指标聚合 + 切自己 ownerId")
        void hr_4_metrics() {
            setAuth(10L, "HR");
            when(statsMapper.countNewApplications(any(OffsetDateTime.class), eq(hrScope(10L)))).thenReturn(20L);
            when(statsMapper.countTransitionsToStage(any(OffsetDateTime.class), eq("OFFER"), eq(hrScope(10L)))).thenReturn(3L);
            when(statsMapper.countTransitionsToStage(any(OffsetDateTime.class), eq("HIRED"), eq(hrScope(10L)))).thenReturn(1L);
            when(statsMapper.countActiveJobs(eq(hrScope(10L)))).thenReturn(7L);

            OverviewVO vo = service.overview();

            assertThat(vo.getNewApplicationsThisMonth()).isEqualTo(20L);
            assertThat(vo.getOffersThisMonth()).isEqualTo(3L);
            assertThat(vo.getHiresThisMonth()).isEqualTo(1L);
            assertThat(vo.getActiveJobs()).isEqualTo(7L);
        }

        @Test
        @DisplayName("ADMIN · ownerId 传 null")
        void admin_null_owner() {
            setAuth(1L, "ADMIN");
            when(statsMapper.countNewApplications(any(OffsetDateTime.class), isNull())).thenReturn(100L);
            when(statsMapper.countTransitionsToStage(any(OffsetDateTime.class), eq("OFFER"), isNull())).thenReturn(15L);
            when(statsMapper.countTransitionsToStage(any(OffsetDateTime.class), eq("HIRED"), isNull())).thenReturn(8L);
            when(statsMapper.countActiveJobs(isNull())).thenReturn(33L);

            OverviewVO vo = service.overview();

            assertThat(vo.getNewApplicationsThisMonth()).isEqualTo(100L);
            assertThat(vo.getActiveJobs()).isEqualTo(33L);
            verify(statsMapper).countActiveJobs(isNull());
        }

        @Test
        @DisplayName("CANDIDATE · 直接 FORBIDDEN")
        void candidate_forbidden() {
            setAuth(99L, "CANDIDATE");
            assertThatThrownBy(() -> service.overview())
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("monthStart · 是本月 1 号 00:00 +08:00")
        void month_start_is_first_day_of_month() {
            OffsetDateTime since = StatsService.monthStart();
            assertThat(since.getDayOfMonth()).isEqualTo(1);
            assertThat(since.getHour()).isZero();
            assertThat(since.getMinute()).isZero();
            assertThat(since.getOffset().getTotalSeconds()).isEqualTo(8 * 3600);
        }
    }

    // ═════════════════════════════ publicStats ═════════════════════════════

    @Nested
    @DisplayName("publicStats · 公开聚合（permitAll）")
    class PublicStats {

        @Test
        @DisplayName("无认证 · 全平台快照（hrUserId 全传 null）")
        void no_auth_global_snapshot() {
            // 不 setAuth：模拟未登录用户访问
            when(applicationMapper.countByStage(isNull(), isNull())).thenReturn(List.of(
                    Map.of("stage", "APPLIED", "cnt", 12L),
                    Map.of("stage", "SCREENING_PASS", "cnt", 5L),
                    Map.of("stage", "PHONE_INTERVIEW", "cnt", 3L),
                    Map.of("stage", "TECH_INTERVIEW", "cnt", 2L),
                    Map.of("stage", "HR_INTERVIEW", "cnt", 1L),
                    Map.of("stage", "OFFER", "cnt", 4L),
                    Map.of("stage", "HIRED", "cnt", 7L),
                    Map.of("stage", "REJECTED", "cnt", 30L)
            ));
            when(statsMapper.countActiveJobs(isNull())).thenReturn(15L);
            when(statsMapper.countCoveredDepartments()).thenReturn(4L);

            PublicStatsVO vo = service.publicStats();

            assertThat(vo.getScreeningCount()).isEqualTo(17L); // 12 + 5
            assertThat(vo.getInterviewCount()).isEqualTo(6L);  // 3 + 2 + 1
            assertThat(vo.getOfferCount()).isEqualTo(4L);
            assertThat(vo.getPublishedJobs()).isEqualTo(15L);
            assertThat(vo.getCoveredDepartments()).isEqualTo(4L);
            // HIRED / REJECTED 不进入水位指标
            verify(statsMapper).countActiveJobs(isNull());
            verify(statsMapper).countCoveredDepartments();
        }

        @Test
        @DisplayName("零数据 · 所有字段为 0（前端会模糊化为'多人/多个'）")
        void zero_data_all_zero() {
            when(applicationMapper.countByStage(isNull(), isNull())).thenReturn(List.of());
            when(statsMapper.countActiveJobs(isNull())).thenReturn(0L);
            when(statsMapper.countCoveredDepartments()).thenReturn(0L);

            PublicStatsVO vo = service.publicStats();

            assertThat(vo.getScreeningCount()).isZero();
            assertThat(vo.getInterviewCount()).isZero();
            assertThat(vo.getOfferCount()).isZero();
            assertThat(vo.getPublishedJobs()).isZero();
            assertThat(vo.getCoveredDepartments()).isZero();
        }

        @Test
        @DisplayName("已登录用户调用 · 同样返回全平台快照（不依赖 SecurityContext）")
        void logged_in_user_also_works() {
            setAuth(99L, "CANDIDATE");
            when(applicationMapper.countByStage(isNull(), isNull())).thenReturn(List.of(
                    Map.of("stage", "APPLIED", "cnt", 1L)
            ));
            when(statsMapper.countActiveJobs(isNull())).thenReturn(0L);
            when(statsMapper.countCoveredDepartments()).thenReturn(0L);

            PublicStatsVO vo = service.publicStats();

            // 登录态不影响（注意：CANDIDATE 调 funnel/overview 会被拒，但 publicStats 是 permitAll）
            assertThat(vo.getScreeningCount()).isEqualTo(1L);
        }
    }

    private void setAuth(long userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
