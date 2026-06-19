package com.ats.file;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;

import java.util.Set;

/**
 * 文件分类 · 控制白名单 + 子目录布局。
 *
 * <p>新增分类时：扩枚举 + 在前端镜像一份即可。每个分类有自己的 MIME 白名单，
 * 防止把 PDF 接口当作通用图床。</p>
 */
public enum FileCategory {

    /** 简历：仅 PDF（候选人投递时上传） */
    RESUME(
            "resumes",
            Set.of("application/pdf"),
            Set.of(".pdf")
    );

    private final String subDir;
    private final Set<String> allowedContentTypes;
    private final Set<String> allowedExtensions;

    FileCategory(String subDir, Set<String> allowedContentTypes, Set<String> allowedExtensions) {
        this.subDir = subDir;
        this.allowedContentTypes = allowedContentTypes;
        this.allowedExtensions = allowedExtensions;
    }

    public String getSubDir() {
        return subDir;
    }

    public Set<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public Set<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    /**
     * 校验 MIME + 扩展名，命中任何一条不通过都抛 {@link ErrorCode#FILE_TYPE_NOT_ALLOWED}。
     *
     * @param contentType {@code MultipartFile.getContentType()} 返回值（浏览器声明，可伪造，但作为第一道防线足够）
     * @param extension   小写的扩展名（含点，如 {@code .pdf}）
     */
    public void validate(String contentType, String extension) {
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                    "仅支持以下 MIME 类型：" + allowedContentTypes);
        }
        if (extension == null || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                    "仅支持以下扩展名：" + allowedExtensions);
        }
    }

    /**
     * 解析字符串到枚举，未命中默认抛参数错误（避免给前端无脑传 "all"）。
     */
    public static FileCategory parse(String category) {
        if (category == null || category.isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "category 不能为空");
        }
        try {
            return FileCategory.valueOf(category.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, "未知的文件分类：" + category);
        }
    }
}
