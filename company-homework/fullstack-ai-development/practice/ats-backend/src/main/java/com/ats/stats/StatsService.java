package com.ats.stats;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.security.SecurityUtil;
import com.ats.entity.ApplicationStage;
import com.ats.job.HrJobScope;
import com.ats.job.HrJobScopeService;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.StatsMapper;
import com.ats.stats.dto.FunnelItemVO;
import com.ats.stats.dto.FunnelVO;
import com.ats.stats.dto.OverviewVO;
import com.ats.stats.dto.PublicStatsVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板 service · M5.1。
 *
 * <h3>权限切片</h3>
 * 与 ApplicationService.board / InterviewService 完全同构：
 * <ul>
 *   <li>ADMIN：全局视角，hrUserId 传 {@code null}</li>
 *   <li>HR：自己视角，hrUserId = 当前用户 id</li>
 *   <li>CANDIDATE：404 不可达（controller 层 {@code @PreAuthorize} 拦截）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final ApplicationMapper applicationMapper;
    private final StatsMapper statsMapper;
    private final HrJobScopeService hrJobScopeService;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private static final String PUBLIC_STATS_CACHE_KEY = "stats:public:v1";
    private static final Duration PUBLIC_STATS_TTL = Duration.ofSeconds(60);

    /** 业务时区与 application.yml 的 jackson.time-zone 对齐 */
    private static final ZoneId BIZ_ZONE = ZoneId.of("Asia/Shanghai");

    /** 漏斗 stage 固定顺序（与看板列同序），缺失 stage 也要补 0 */
    private static final List<ApplicationStage> FUNNEL_ORDER = List.of(
            ApplicationStage.APPLIED,
            ApplicationStage.SCREENING_PASS,
            ApplicationStage.PHONE_INTERVIEW,
            ApplicationStage.TECH_INTERVIEW,
            ApplicationStage.HR_INTERVIEW,
            ApplicationStage.OFFER,
            ApplicationStage.HIRED,
            ApplicationStage.REJECTED
    );

    public FunnelVO funnel(Long jobId) {
        HrJobScope hrScope = effectiveHrScope();
        List<Map<String, Object>> rows = applicationMapper.countByStage(jobId, hrScope);

        Map<ApplicationStage, Long> counts = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String stageStr = (String) row.get("stage");
            if (stageStr == null) continue;
            Object cntObj = row.get("cnt");
            long cnt = cntObj instanceof Number n ? n.longValue() : 0L;
            counts.put(ApplicationStage.valueOf(stageStr), cnt);
        }

        List<FunnelItemVO> items = new ArrayList<>(FUNNEL_ORDER.size());
        long total = 0;
        long max = 0;
        for (ApplicationStage st : FUNNEL_ORDER) {
            long c = counts.getOrDefault(st, 0L);
            items.add(FunnelItemVO.builder().stage(st).count(c).build());
            total += c;
            max = Math.max(max, c);
        }

        return FunnelVO.builder()
                .items(items)
                .total(total)
                .max(max)
                .build();
    }

    /**
     * 公开聚合统计（permitAll 接口） —— 不调 effectiveHrUserId，直接传 null 看全平台快照。
     * <p>
     * 复用 {@link ApplicationMapper#countByStage(Long, Long)} 拿全量 stage 分布，
     * 在 service 内做 5 stage group 聚合，避免新增 mapper 方法。
     */
    public PublicStatsVO publicStats() {
        String cached = redis.opsForValue().get(PUBLIC_STATS_CACHE_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, PublicStatsVO.class);
            }
            catch (JsonProcessingException e) {
                log.warn("[STATS] public cache deserialize failed, recompute");
            }
        }
        PublicStatsVO vo = computePublicStats();
        try {
            redis.opsForValue().set(PUBLIC_STATS_CACHE_KEY,
                    objectMapper.writeValueAsString(vo), PUBLIC_STATS_TTL);
        }
        catch (JsonProcessingException e) {
            log.warn("[STATS] public cache serialize failed");
        }
        return vo;
    }

    private PublicStatsVO computePublicStats() {
        List<Map<String, Object>> rows = applicationMapper.countByStage(null, null);
        long screening = 0;
        long interview = 0;
        long offer = 0;
        for (Map<String, Object> row : rows) {
            String stageStr = (String) row.get("stage");
            if (stageStr == null) continue;
            long cnt = row.get("cnt") instanceof Number n ? n.longValue() : 0L;
            ApplicationStage stage = ApplicationStage.valueOf(stageStr);
            switch (stage) {
                case APPLIED, SCREENING_PASS -> screening += cnt;
                case PHONE_INTERVIEW, TECH_INTERVIEW, HR_INTERVIEW -> interview += cnt;
                case OFFER -> offer += cnt;
                default -> {
                    // HIRED / REJECTED 不计入 "进行中" 水位
                }
            }
        }

        return PublicStatsVO.builder()
                .screeningCount(screening)
                .interviewCount(interview)
                .offerCount(offer)
                .publishedJobs(statsMapper.countActiveJobs(null))
                .coveredDepartments(statsMapper.countCoveredDepartments())
                .build();
    }

    /** 导出漏斗 CSV（HR/ADMIN）。 */
    public String exportFunnelCsv(Long jobId) {
        FunnelVO funnel = funnel(jobId);
        StringBuilder sb = new StringBuilder("stage,count\n");
        for (var item : funnel.getItems()) {
            sb.append(item.getStage().name()).append(',').append(item.getCount()).append('\n');
        }
        return sb.toString();
    }

    public OverviewVO overview() {
        HrJobScope hrScope = effectiveHrScope();
        OffsetDateTime since = monthStart();

        long newApps = statsMapper.countNewApplications(since, hrScope);
        long offers = statsMapper.countTransitionsToStage(since, ApplicationStage.OFFER.name(), hrScope);
        long hires = statsMapper.countTransitionsToStage(since, ApplicationStage.HIRED.name(), hrScope);
        long activeJobs = statsMapper.countActiveJobs(hrScope);

        return OverviewVO.builder()
                .newApplicationsThisMonth(newApps)
                .offersThisMonth(offers)
                .hiresThisMonth(hires)
                .activeJobs(activeJobs)
                .build();
    }

    /**
     * 当前用户切片 ID：
     * - ADMIN 返回 null（看全部）
     * - HR 返回自己的 userId
     * - 其他角色应被 controller 拦截不到此处；保险起见抛 FORBIDDEN
     */
    HrJobScope effectiveHrScope() {
        if (SecurityUtil.isAdmin()) return null;
        if (SecurityUtil.isHr()) return hrJobScopeService.currentScopeOrNull();
        throw BizException.of(ErrorCode.FORBIDDEN);
    }

    /**
     * 业务月份起点（本月 1 号 00:00 ZoneId=Asia/Shanghai 对应的 OffsetDateTime）。
     * 用 service 内部计算而非 SQL 内 NOW() 是为了：(1) 测试可控；(2) 避免 DB 时区漂移。
     */
    static OffsetDateTime monthStart() {
        LocalDate firstOfMonth = LocalDate.now(BIZ_ZONE).withDayOfMonth(1);
        return firstOfMonth.atStartOfDay(BIZ_ZONE).toOffsetDateTime();
    }

    // 非业务方法：让单测能直接验证 stage 顺序
    static List<ApplicationStage> funnelOrderForTest() {
        return FUNNEL_ORDER.stream().sorted(Comparator.naturalOrder()).toList();
    }
}
