package com.ats.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 整个看板：8 列状态机 + 范围（jobId or null = 全部我的岗位）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardVO {
    /** 单岗位看板时填岗位 id；跨岗位看板（HR 名下所有）此处为 null */
    private Long jobId;
    private String jobTitle;
    /** 每列固定按状态机顺序返回，前端无需再排序 */
    private List<BoardColumnVO> columns;
    private long totalApplications;
    /** 岗位筛选命中数达到上限（默认 500），结果可能被截断 */
    private boolean jobsTruncated;
}
