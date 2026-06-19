package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.file.FileService;
import com.ats.file.dto.UploadVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件上传 / 下载 endpoint。
 *
 * <h3>鉴权</h3>
 * <ul>
 *   <li>{@code POST /files/upload}：任意 JWT 用户（候选人投简 / HR 也可能上传其他东西）</li>
 *   <li>{@code GET /files/**}：任意 JWT 用户。文件名是 UUID v4 不可枚举，URL 只在
 *       application 当事人 / 该岗位 HR / ADMIN 三方可见，业务上自然形成访问边界。
 *       生产环境如需更严格，再追加「按 url 反查 application 鉴权」。</li>
 * </ul>
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 文件上传：multipart/form-data，字段名 {@code file}，分类用 {@code category}（如 {@code RESUME}）。
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "RESUME") String category
    ) {
        return ApiResponse.ok(fileService.upload(category, file));
    }

    /**
     * 流式下载/预览。前端通常用 {@code <a target="_blank" :href="url">} 即可，浏览器会按
     * Content-Type 决定预览还是下载。
     *
     * <p>用 {@code /**} 通配 + {@code HttpServletRequest.getRequestURI()} 取实际请求路径，
     * 然后去掉 contextPath（生产环境是 {@code /api/v1}），最后把 {@code /files/} 前缀替换为 {@code /uploads/} 交给 service 解析。
     * <strong>注意</strong>：用 {@code getServletPath()} 在某些容器配置下会返回 null/空（M4 踩坑），
     * {@code getRequestURI()} 始终返回原始 URI 更稳。</p>
     */
    @GetMapping("/**")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        // uri 例：/files/resumes/2026-05/xxx.pdf
        String url = uri.startsWith("/files/")
                ? "/uploads/" + uri.substring("/files/".length())
                : uri;
        FileService.LoadedResource loaded = fileService.loadByUrl(url);
        Resource res = loaded.resource();

        String filename = res.getFilename() != null ? res.getFilename() : "download";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(loaded.contentType()))
                // inline 让浏览器直接预览 PDF；如要强制下载改 attachment
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded)
                .body(res);
    }
}
