package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.job.DepartmentService;
import com.ats.job.dto.SubDepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 子部门字典：公开 GET 接口（HR 新建/编辑岗位下拉、看板筛选下拉都用）。
 * <p>
 * M6 新增：原 /departments 返回中间节点，新建岗位实际需要叶子节点 + 工作地点。
 * 同时返回上层部门名与根组织名，前端可拼接为「子部门 / 工作地点」展示。
 */
@RestController
@RequestMapping("/sub-departments")
@RequiredArgsConstructor
public class SubDepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<SubDepartmentVO>> list() {
        return ApiResponse.ok(departmentService.listAllSubDepartments());
    }
}
