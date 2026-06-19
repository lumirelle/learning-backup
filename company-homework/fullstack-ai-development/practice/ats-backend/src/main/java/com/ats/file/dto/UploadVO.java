package com.ats.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传成功响应。前端拿到 url 后写入 application.resumeUrl。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadVO {

    /** 对外可见的相对 URL，如 {@code /uploads/resumes/2026-05/<uuid>.pdf} */
    private String url;

    /** 文件大小（字节） */
    private long size;

    /** 原始文件名（仅展示用，不参与路径） */
    private String originalName;

    /** 服务端识别的 Content-Type */
    private String contentType;
}
