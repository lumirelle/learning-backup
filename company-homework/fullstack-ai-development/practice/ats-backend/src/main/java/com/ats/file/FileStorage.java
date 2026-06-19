package com.ats.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象 · MVP 用 LocalFileStorage 实现，后续可换 S3 / OSS / MinIO 而不动调用方。
 */
public interface FileStorage {

    /**
     * 把上传文件保存到分类目录下，返回**对外可见的相对 URL**（如 {@code /uploads/resumes/2026-05/<uuid>.pdf}）。
     * 此 URL 是 controller {@code GET /files/...} 的入参（去掉 {@code /uploads/} 前缀后即为资源路径）。
     */
    String save(FileCategory category, MultipartFile file);

    /**
     * 加载资源（用于 GET /files/{...} 流式下载/预览）。
     *
     * @param relativePath 相对 {@code uploadRoot} 的路径，如 {@code resumes/2026-05/<uuid>.pdf}
     * @return Resource，调用方 stream 出去即可
     */
    Resource load(String relativePath);

    /**
     * 读取资源时的 Content-Type（用于 HTTP header）。
     */
    String detectContentType(String relativePath);
}
