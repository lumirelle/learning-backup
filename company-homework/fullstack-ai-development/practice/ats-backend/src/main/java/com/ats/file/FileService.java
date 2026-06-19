package com.ats.file;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.file.dto.UploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件 service · 对外门面。
 *
 * <p>分两层校验：
 * <ol>
 *   <li>{@code spring.servlet.multipart.max-file-size} 在 servlet 层硬拦截，超限抛 {@link org.springframework.web.multipart.MaxUploadSizeExceededException}</li>
 *   <li>本 service 再用 {@link UploadProperties#getMaxFileMb()} 复检，给统一的 {@link ErrorCode#FILE_TOO_LARGE} 业务码</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorage storage;
    private final UploadProperties props;

    public UploadVO upload(String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件为空");
        }
        long maxBytes = (long) props.getMaxFileMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BizException(ErrorCode.FILE_TOO_LARGE,
                    "文件超出大小限制（最大 " + props.getMaxFileMb() + "MB）");
        }
        FileCategory cat = FileCategory.parse(category);
        String url = storage.save(cat, file);
        return UploadVO.builder()
                .url(url)
                .size(file.getSize())
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * 把对外 URL（如 {@code /uploads/resumes/2026-05/<uuid>.pdf}）转成相对 root 的资源路径
     * （{@code resumes/2026-05/<uuid>.pdf}），交给 storage.load。
     */
    public LoadedResource loadByUrl(String url) {
        String rel = stripUploadPrefix(url);
        Resource res = storage.load(rel);
        String contentType = storage.detectContentType(rel);
        return new LoadedResource(res, contentType, rel);
    }

    static String stripUploadPrefix(String url) {
        if (url == null || url.isBlank()) {
            throw new BizException(ErrorCode.FILE_NOT_FOUND, "URL 为空");
        }
        String s = url.trim();
        // 兼容传 "uploads/resumes/..." 或 "/uploads/resumes/..." 或 "resumes/..."
        if (s.startsWith("/uploads/")) return s.substring("/uploads/".length());
        if (s.startsWith("uploads/"))  return s.substring("uploads/".length());
        return s.startsWith("/") ? s.substring(1) : s;
    }

    public record LoadedResource(Resource resource, String contentType, String relativePath) {}
}
