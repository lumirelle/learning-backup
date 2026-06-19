package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.job.DepartmentService;
import com.ats.job.dto.DepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门字典：公开 GET 接口（候选人筛选 + HR 看板筛选 + HR 新建/编辑岗位都用）。
 */
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<DepartmentVO>> list() {
        return ApiResponse.ok(departmentService.listAll());
    }
}
