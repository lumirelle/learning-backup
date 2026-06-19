package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.stats.StatsService;
import com.ats.stats.dto.FunnelVO;
import com.ats.stats.dto.OverviewVO;
import com.ats.stats.dto.PublicStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板 endpoint · M5.1。HR / ADMIN 路径 + 公开 /public 子路径。
 */
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 招聘漏斗 · 8 态 application 计数。
     *
     * @param jobId 可选，传则限定到该岗位；不传则按 HR 视角看自己所有岗位 / ADMIN 看全部
     */
    @GetMapping("/funnel")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<FunnelVO> funnel(@RequestParam(required = false) Long jobId) {
        return ApiResponse.ok(statsService.funnel(jobId));
    }

    /** 本月概览 · 4 个核心指标。 */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<OverviewVO> overview() {
        return ApiResponse.ok(statsService.overview());
    }

    /**
     * 公开聚合统计（permitAll，未登录可访问）—— 用于登录 / 注册页"水位"展示。
     * <p>
     * 仅返回聚合数字，不暴露任何个体；前端会对 &lt; 5 的小数模糊化为"多人 / 多个"。
     * 在 {@link com.ats.config.SecurityConfig} 通过路径 permitAll 放行，绕过 JWT 过滤。
     */
    @GetMapping("/public")
    public ApiResponse<PublicStatsVO> publicStats() {
        return ApiResponse.ok(statsService.publicStats());
    }

    @GetMapping(value = "/funnel/export", produces = "text/csv")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<String> exportFunnel(@RequestParam(required = false) Long jobId) {
        String csv = statsService.exportFunnelCsv(jobId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=funnel.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
