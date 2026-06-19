package com.ats.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件上传配置（绑定 application.yml 的 ats.upload.*）。
 *
 * <h3>路径基准</h3>
 * <ul>
 *   <li>{@code path} 为相对路径时（如 {@code ./uploads}），基于后端启动 cwd 解析</li>
 *   <li>生产环境通过 ENV 覆盖到挂载卷（如 {@code /var/lib/ats/uploads}）</li>
 * </ul>
 *
 * <h3>大小限制</h3>
 * 与 {@code spring.servlet.multipart.max-file-size} 共用 ENV {@code UPLOAD_MAX_FILE_MB}，
 * 双向对齐：multipart 在 servlet 层做硬拦截 → 抛 MaxUploadSizeExceededException → 413；
 * service 层再用 {@code maxFileMb} 做次校验，给统一的 FILE_TOO_LARGE 业务错误码。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ats.upload")
public class UploadProperties {

    /** 上传根目录（resumes/ 等子目录在其下创建） */
    private String path = "./uploads";

    /** 单文件最大大小，单位 MB */
    private int maxFileMb = 5;
}
