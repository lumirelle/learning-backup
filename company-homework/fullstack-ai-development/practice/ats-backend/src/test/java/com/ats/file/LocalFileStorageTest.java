package com.ats.file;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LocalFileStorage 单测：写入正常路径 / 类型校验 / 路径穿越 / 扩展名提取。
 */
@DisplayName("LocalFileStorage · 文件存储")
class LocalFileStorageTest {

    @Test
    @DisplayName("save · 合法 PDF → 落盘 + 返回 /uploads/resumes/yyyy-MM/<uuid>.pdf")
    void save_legal_pdf(@TempDir Path tmp) {
        UploadProperties props = new UploadProperties();
        props.setPath(tmp.toString());
        FileDedupIndex dedup = new FileDedupIndex(props, new ObjectMapper());
        LocalFileStorage storage = new LocalFileStorage(props, dedup);

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "my-resume.pdf", "application/pdf", "%PDF-1.4 ...".getBytes());

        String url = storage.save(FileCategory.RESUME, pdf);

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        assertThat(url)
                .startsWith("/uploads/resumes/" + month + "/")
                .endsWith(".pdf");

        // 物理文件应该存在
        String rel = url.substring("/uploads/".length());
        assertThat(Files.exists(tmp.resolve(rel))).isTrue();
    }

    @Test
    @DisplayName("save · 非 PDF MIME → FILE_TYPE_NOT_ALLOWED")
    void save_wrong_mime(@TempDir Path tmp) {
        UploadProperties props = new UploadProperties();
        props.setPath(tmp.toString());
        FileDedupIndex dedup = new FileDedupIndex(props, new ObjectMapper());
        LocalFileStorage storage = new LocalFileStorage(props, dedup);

        MockMultipartFile png = new MockMultipartFile(
                "file", "x.pdf", "image/png", "fakebytes".getBytes());

        assertThatThrownBy(() -> storage.save(FileCategory.RESUME, png))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("save · 扩展名不是 .pdf → FILE_TYPE_NOT_ALLOWED")
    void save_wrong_ext(@TempDir Path tmp) {
        UploadProperties props = new UploadProperties();
        props.setPath(tmp.toString());
        FileDedupIndex dedup = new FileDedupIndex(props, new ObjectMapper());
        LocalFileStorage storage = new LocalFileStorage(props, dedup);

        MockMultipartFile bad = new MockMultipartFile(
                "file", "x.docx", "application/pdf", "fakebytes".getBytes());

        assertThatThrownBy(() -> storage.save(FileCategory.RESUME, bad))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("save · 空文件 → BAD_REQUEST")
    void save_empty(@TempDir Path tmp) {
        UploadProperties props = new UploadProperties();
        props.setPath(tmp.toString());
        FileDedupIndex dedup = new FileDedupIndex(props, new ObjectMapper());
        LocalFileStorage storage = new LocalFileStorage(props, dedup);

        MockMultipartFile empty = new MockMultipartFile(
                "file", "x.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> storage.save(FileCategory.RESUME, empty))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("load · 文件不存在 → FILE_NOT_FOUND")
    void load_not_found(@TempDir Path tmp) {
        UploadProperties props = new UploadProperties();
        props.setPath(tmp.toString());
        FileDedupIndex dedup = new FileDedupIndex(props, new ObjectMapper());
        LocalFileStorage storage = new LocalFileStorage(props, dedup);

        assertThatThrownBy(() -> storage.load("resumes/2026-05/nope.pdf"))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("load · 路径穿越 ../ → FILE_NOT_FOUND（不会读到 root 外的内容）")
    void load_traversal(@TempDir Path tmp) throws IOException {
        // 在 root 之外造一个文件
        Path outside = tmp.getParent().resolve("secret.txt");
        Files.writeString(outside, "secret");

        UploadProperties props = new UploadProperties();
        props.setPath(tmp.toString());
        FileDedupIndex dedup = new FileDedupIndex(props, new ObjectMapper());
        LocalFileStorage storage = new LocalFileStorage(props, dedup);

        assertThatThrownBy(() -> storage.load("../secret.txt"))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("load · 合法路径 → Resource 可读")
    void load_ok(@TempDir Path tmp) throws IOException {
        UploadProperties props = new UploadProperties();
        props.setPath(tmp.toString());
        FileDedupIndex dedup = new FileDedupIndex(props, new ObjectMapper());
        LocalFileStorage storage = new LocalFileStorage(props, dedup);

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "my.pdf", "application/pdf", "hello-pdf".getBytes());
        String url = storage.save(FileCategory.RESUME, pdf);
        String rel = url.substring("/uploads/".length());

        Resource res = storage.load(rel);
        assertThat(res.exists()).isTrue();
        assertThat(res.isReadable()).isTrue();
        // try-with-resources 关闭 InputStream，避免 Windows 下 TempDir 清理失败（UrlResource 持有 file handle）
        try (InputStream is = res.getInputStream()) {
            assertThat(new String(is.readAllBytes())).isEqualTo("hello-pdf");
        }
    }

    @Test
    @DisplayName("extractExtension · 各种边界值")
    void extractExtension_cases() {
        assertThat(LocalFileStorage.extractExtension("a.PDF")).isEqualTo(".pdf");
        assertThat(LocalFileStorage.extractExtension("foo")).isEmpty();
        assertThat(LocalFileStorage.extractExtension("")).isEmpty();
        assertThat(LocalFileStorage.extractExtension(null)).isEmpty();
        assertThat(LocalFileStorage.extractExtension("a.")).isEmpty();
        assertThat(LocalFileStorage.extractExtension("a.b.pdf")).isEqualTo(".pdf");
        assertThat(LocalFileStorage.extractExtension("../etc/passwd.pdf")).isEqualTo(".pdf");
        assertThat(LocalFileStorage.extractExtension("c:\\path\\file.PDF")).isEqualTo(".pdf");
    }
}
