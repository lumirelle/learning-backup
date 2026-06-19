package com.ats.web;

import com.ats.auth.JwtAuthEntryPoint;
import com.ats.auth.JwtAuthenticationFilter;
import com.ats.auth.JwtService;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.exception.GlobalExceptionHandler;
import com.ats.config.SecurityConfig;
import com.ats.controller.FileController;
import com.ats.file.FileService;
import com.ats.file.dto.UploadVO;
import com.ats.repository.ApplicationMapper;
import com.ats.repository.InterviewMapper;
import com.ats.repository.JobMapper;
import com.ats.repository.JobTagMapper;
import com.ats.repository.RefreshTokenMapper;
import com.ats.repository.StageLogMapper;
import com.ats.repository.TagMapper;
import com.ats.repository.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FileController 权限矩阵 + 错误码集成测试（M4）。
 *
 * <ul>
 *   <li><b>POST /files/upload</b>：任意 JWT 用户（401 / 200）；service 错误码透传</li>
 *   <li><b>GET /files/...</b>：任意 JWT 用户；FILE_NOT_FOUND → 404；FILE_TOO_LARGE 不在此路径</li>
 * </ul>
 */
@WebMvcTest(controllers = FileController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthEntryPoint.class,
        GlobalExceptionHandler.class,
})
@ActiveProfiles("test")
@DisplayName("FileController · 权限矩阵 + 错误码")
class FileControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean FileService fileService;

    // 全部 mapper（@MapperScan 扫到的）+ PasswordEncoder
    @MockitoBean JobMapper jobMapper;
    @MockitoBean TagMapper tagMapper;
    @MockitoBean JobTagMapper jobTagMapper;
    @MockitoBean UserMapper userMapper;
    @MockitoBean RefreshTokenMapper refreshTokenMapper;
    @MockitoBean ApplicationMapper applicationMapper;
    @MockitoBean StageLogMapper stageLogMapper;
    @MockitoBean InterviewMapper interviewMapper;
    @MockitoBean com.ats.repository.StatsMapper statsMapper;
    @MockitoBean com.ats.repository.RootOrgMapper rootOrgMapper;
    @MockitoBean com.ats.repository.DepartmentMapper departmentMapper;
    @MockitoBean com.ats.repository.SubDepartmentMapper subDepartmentMapper;
    @MockitoBean com.ats.repository.HrSubDepartmentMapper hrSubDepartmentMapper;
    @MockitoBean org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("POST /files/upload")
    class Upload {

        @Test
        @DisplayName("匿名 → 401，不调 service")
        void anonymous_401() throws Exception {
            MockMultipartFile pdf = new MockMultipartFile(
                    "file", "x.pdf", "application/pdf", "hello".getBytes());
            mvc.perform(MockMvcRequestBuilders.multipart("/files/upload").file(pdf))
                    .andExpect(status().isUnauthorized());
            verify(fileService, never()).upload(anyString(), any());
        }

        @Test
        @DisplayName("CANDIDATE → 200，返回 url")
        void candidate_200() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(fileService.upload(eq("RESUME"), any())).thenReturn(
                    UploadVO.builder().url("/uploads/resumes/2026-05/abc.pdf")
                            .size(123).originalName("x.pdf").contentType("application/pdf").build());

            MockMultipartFile pdf = new MockMultipartFile(
                    "file", "x.pdf", "application/pdf", "hello".getBytes());
            mvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                            .file(pdf)
                            .param("category", "RESUME")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.url").value("/uploads/resumes/2026-05/abc.pdf"))
                    .andExpect(jsonPath("$.data.size").value(123));
        }

        @Test
        @DisplayName("HR → 200（任意 JWT 都可上传）")
        void hr_200() throws Exception {
            mockClaims("hr-tok", 10L, "HR");
            when(fileService.upload(any(), any())).thenReturn(
                    UploadVO.builder().url("/uploads/resumes/2026-05/x.pdf").build());

            MockMultipartFile pdf = new MockMultipartFile(
                    "file", "x.pdf", "application/pdf", "hello".getBytes());
            mvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                            .file(pdf)
                            .header("Authorization", "Bearer hr-tok"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("非 PDF MIME → service 抛 FILE_TYPE_NOT_ALLOWED → 415 Unsupported Media Type")
        void wrongType_415() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(fileService.upload(any(), any()))
                    .thenThrow(new BizException(ErrorCode.FILE_TYPE_NOT_ALLOWED));

            MockMultipartFile png = new MockMultipartFile(
                    "file", "x.png", "image/png", "hello".getBytes());
            mvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                            .file(png)
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FILE_TYPE_NOT_ALLOWED.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /files/**")
    class Download {

        @Test
        @DisplayName("匿名 → 401")
        void anonymous_401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/files/resumes/2026-05/x.pdf"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("CANDIDATE 合法路径 → 200 + Content-Type")
        void candidate_200() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            ByteArrayResource fakePdf = new ByteArrayResource("%PDF-1.4 ...".getBytes()) {
                @Override public String getFilename() { return "x.pdf"; }
            };
            when(fileService.loadByUrl(eq("/uploads/resumes/2026-05/x.pdf")))
                    .thenReturn(new FileService.LoadedResource(fakePdf, "application/pdf", "resumes/2026-05/x.pdf"));

            mvc.perform(MockMvcRequestBuilders.get("/files/resumes/2026-05/x.pdf")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("FILE_NOT_FOUND → 404")
        void notFound_404() throws Exception {
            mockClaims("c-tok", 99L, "CANDIDATE");
            when(fileService.loadByUrl(anyString()))
                    .thenThrow(new BizException(ErrorCode.FILE_NOT_FOUND));

            mvc.perform(MockMvcRequestBuilders.get("/files/resumes/2026-05/nope.pdf")
                            .header("Authorization", "Bearer c-tok"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FILE_NOT_FOUND.getCode()));
        }
    }

    private void mockClaims(String token, long userId, String role) {
        HashMap<String, Object> raw = new HashMap<>();
        raw.put(Claims.SUBJECT, String.valueOf(userId));
        raw.put("email", "u" + userId + "@b.com");
        raw.put("role", role);
        when(jwtService.verifyAccessToken(token)).thenReturn(new DefaultClaims(raw));
    }
}
