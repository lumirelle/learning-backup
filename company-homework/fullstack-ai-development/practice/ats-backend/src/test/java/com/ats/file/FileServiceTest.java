package com.ats.file;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.file.dto.UploadVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FileService 业务校验：大小 / 分类 / URL 前缀剥离。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileService · 业务规则")
class FileServiceTest {

    @Mock FileStorage storage;

    UploadProperties props;
    FileService svc;

    @BeforeEach
    void setUp() {
        props = new UploadProperties();
        props.setPath("./uploads");
        props.setMaxFileMb(5);
        svc = new FileService(storage, props);
    }

    @Test
    @DisplayName("upload · 合法 PDF → 透传 storage.save，返回 UploadVO")
    void upload_ok() {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "x.pdf", "application/pdf", "hello".getBytes());
        when(storage.save(eq(FileCategory.RESUME), any())).thenReturn("/uploads/resumes/2026-05/abc.pdf");

        UploadVO vo = svc.upload("RESUME", pdf);

        assertThat(vo.getUrl()).isEqualTo("/uploads/resumes/2026-05/abc.pdf");
        assertThat(vo.getSize()).isEqualTo(5);
        assertThat(vo.getOriginalName()).isEqualTo("x.pdf");
        assertThat(vo.getContentType()).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("upload · 大小超 maxFileMb → FILE_TOO_LARGE，不调 storage")
    void upload_too_large() {
        byte[] huge = new byte[6 * 1024 * 1024]; // 6MB > 5MB
        MockMultipartFile big = new MockMultipartFile(
                "file", "x.pdf", "application/pdf", huge);

        assertThatThrownBy(() -> svc.upload("RESUME", big))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_TOO_LARGE);
        verify(storage, never()).save(any(), any());
    }

    @Test
    @DisplayName("upload · 空文件 → BAD_REQUEST")
    void upload_empty() {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "x.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> svc.upload("RESUME", empty))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("upload · category 非法 → BAD_REQUEST")
    void upload_bad_category() {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "x.pdf", "application/pdf", "hello".getBytes());

        assertThatThrownBy(() -> svc.upload("FOO", pdf))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("stripUploadPrefix · 多种 URL 前缀都能正确剥离")
    void strip_prefix_cases() {
        assertThat(FileService.stripUploadPrefix("/uploads/resumes/x.pdf")).isEqualTo("resumes/x.pdf");
        assertThat(FileService.stripUploadPrefix("uploads/resumes/x.pdf")).isEqualTo("resumes/x.pdf");
        assertThat(FileService.stripUploadPrefix("/resumes/x.pdf")).isEqualTo("resumes/x.pdf");
        assertThat(FileService.stripUploadPrefix("resumes/x.pdf")).isEqualTo("resumes/x.pdf");
    }

    @Test
    @DisplayName("stripUploadPrefix · 空串 → FILE_NOT_FOUND")
    void strip_prefix_empty() {
        assertThatThrownBy(() -> FileService.stripUploadPrefix(""))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
        assertThatThrownBy(() -> FileService.stripUploadPrefix(null))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
    }
}
