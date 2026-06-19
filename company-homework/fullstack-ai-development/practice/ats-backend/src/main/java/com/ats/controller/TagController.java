package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.job.TagService;
import com.ats.job.dto.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /** 标签库全量列表，公开接口（候选人筛选 + HR 新建岗位都用）。 */
    @GetMapping
    public ApiResponse<List<TagVO>> list() {
        return ApiResponse.ok(tagService.listAll());
    }
}
