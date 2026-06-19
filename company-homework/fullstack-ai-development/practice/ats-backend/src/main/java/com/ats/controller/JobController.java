package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.job.JobService;
import com.ats.job.JobService.PageResult;
import com.ats.job.dto.JobCreateReq;
import com.ats.job.dto.JobDetailVO;
import com.ats.job.dto.JobListItemVO;
import com.ats.job.dto.JobListReq;
import com.ats.job.dto.JobTransitionReq;
import com.ats.job.dto.JobUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /** 列表查询，公开接口。Service 内部按角色裁剪可见状态。 */
    @GetMapping
    public ApiResponse<PageResult<JobListItemVO>> list(@Valid @ModelAttribute JobListReq req) {
        return ApiResponse.ok(jobService.list(req));
    }

    /** 详情，公开接口。Service 校验候选人只能看 PUBLISHED/PAUSED/CLOSED。 */
    @GetMapping("/{id}")
    public ApiResponse<JobDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(jobService.getDetail(id));
    }

    /** 创建岗位（草稿态）。HR / Admin */
    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<JobDetailVO> create(@Valid @RequestBody JobCreateReq req) {
        return ApiResponse.ok(jobService.create(req));
    }

    /** 修改岗位基础字段（不含状态）。HR own / Admin */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<JobDetailVO> update(@PathVariable Long id,
                                           @Valid @RequestBody JobUpdateReq req) {
        return ApiResponse.ok(jobService.update(id, req));
    }

    /** 状态机推进。HR own / Admin */
    @PostMapping("/{id}/transitions")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<JobDetailVO> transition(@PathVariable Long id,
                                               @Valid @RequestBody JobTransitionReq req) {
        return ApiResponse.ok(jobService.transition(id, req.getTo()));
    }

    /** 软删岗位。Admin only */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        jobService.softDelete(id);
        return ApiResponse.ok();
    }
}
