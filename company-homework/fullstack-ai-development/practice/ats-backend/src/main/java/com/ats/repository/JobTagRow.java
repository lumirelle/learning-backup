package com.ats.repository;

import lombok.Data;

/**
 * findTagsByJobIds 的扁平投影行：把 jobId 和 tag 字段一起返回，
 * Service 层按 jobId 分桶后再组装 List<TagVO>。
 */
@Data
public class JobTagRow {
    private Long jobId;
    private Long id;
    private String slug;
    private String name;
    private String category;
}
