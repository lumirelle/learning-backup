package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.interview.InterviewService;
import com.ats.interview.dto.InterviewCreateReq;
import com.ats.interview.dto.InterviewUpdateReq;
import com.ats.interview.dto.InterviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 面试评价 endpoint · M4。
 *
 * <p>路由分两段：</p>
 * <ul>
 *   <li>{@code /applications/{id}/interviews} 集合：list / create</li>
 *   <li>{@code /interviews/{id}} 单条：update（编辑窗口内）</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/applications/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<List<InterviewVO>> list(@PathVariable Long applicationId) {
        return ApiResponse.ok(interviewService.listByApplication(applicationId));
    }

    @PostMapping("/applications/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<InterviewVO> create(
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewCreateReq req
    ) {
        return ApiResponse.ok(interviewService.add(applicationId, req));
    }

    @PutMapping("/interviews/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ApiResponse<InterviewVO> update(
            @PathVariable Long id,
            @Valid @RequestBody InterviewUpdateReq req
    ) {
        return ApiResponse.ok(interviewService.update(id, req));
    }
}
