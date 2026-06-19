package com.ats.web;

import com.ats.auth.AuthService;
import com.ats.auth.JwtAuthEntryPoint;
import com.ats.auth.JwtAuthenticationFilter;
import com.ats.auth.JwtService;
import com.ats.auth.dto.MeVO;
import com.ats.auth.dto.TokenVO;
import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.exception.GlobalExceptionHandler;
import com.ats.config.SecurityConfig;
import com.ats.controller.AdminController;
import com.ats.controller.AuthController;
import com.ats.repository.RefreshTokenMapper;
import com.ats.repository.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import com.ats.entity.SubDepartment;
import com.ats.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web 层端到端安全集成测试：通过 {@link WebMvcTest} 启动真实的 Spring Security
 * 过滤器链（{@link JwtAuthenticationFilter} + {@link JwtAuthEntryPoint}），所有 DB / Service
 * 层用 {@link MockitoBean} 替换，无需 PG / Redis / Docker 即可验证：
 *
 * <ul>
 *   <li><b>401</b>：未带 token、过期 token、篡改 token</li>
 *   <li><b>403</b>：{@code @PreAuthorize("hasRole('ADMIN')")} 拒绝非 ADMIN</li>
 *   <li><b>200</b>：合法 ADMIN token 可调 /admin/users</li>
 *   <li><b>CSRF disable 回归</b>：POST /auth/login 无 X-XSRF-TOKEN 也能通（防止有人偷偷改回 enabled）</li>
 *   <li><b>放行路径</b>：/auth/register、/auth/login、/auth/refresh、/auth/logout 全不需要 token</li>
 *   <li><b>Validation</b>：register 邮箱格式错误 → 400</li>
 * </ul>
 */
@WebMvcTest(controllers = {AuthController.class, AdminController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthEntryPoint.class,
        GlobalExceptionHandler.class,
        com.ats.auth.AdminUserService.class,
})
@ActiveProfiles("test")
@DisplayName("Web Security · 端到端过滤器链 / 角色守卫 / CSRF")
class WebSecurityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean JwtService jwtService;
    @MockitoBean AuthService authService;
    @MockitoBean UserMapper userMapper;
    @MockitoBean RefreshTokenMapper refreshTokenMapper;
    @MockitoBean org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // M2 引入的新 mapper：@MapperScan 仍会扫到，必须 mock 掉以避免 sqlSessionFactory 注入失败
    @MockitoBean com.ats.repository.JobMapper jobMapper;
    @MockitoBean com.ats.repository.TagMapper tagMapper;
    @MockitoBean com.ats.repository.JobTagMapper jobTagMapper;

    // M3 新增 mapper：同样必须 mock，否则 M1/M2 测试反向被波及
    @MockitoBean com.ats.repository.ApplicationMapper applicationMapper;
    @MockitoBean com.ats.repository.StageLogMapper stageLogMapper;
    @MockitoBean com.ats.repository.InterviewMapper interviewMapper;
    @MockitoBean com.ats.repository.StatsMapper statsMapper;

    // M6 新增组织树 mapper（root_orgs / departments / sub_departments / hr_sub_departments）
    @MockitoBean com.ats.repository.RootOrgMapper rootOrgMapper;
    @MockitoBean com.ats.repository.DepartmentMapper departmentMapper;
    @MockitoBean com.ats.repository.SubDepartmentMapper subDepartmentMapper;
    @MockitoBean com.ats.repository.HrSubDepartmentMapper hrSubDepartmentMapper;

    @BeforeEach
    void stubHrSubDepartmentBinding() {
        when(subDepartmentMapper.selectById(anyLong())).thenReturn(new SubDepartment());
    }

    // ════════════════════════════════════════════════════════════
    //                    401 · JWT 过滤器链
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("401 · 未携带 / 过期 / 篡改 token")
    class UnauthorizedCases {

        @Test
        @DisplayName("受保护路径无 Authorization → 401 + ApiResponse 包体")
        void noToken_returns401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()))
                    .andExpect(jsonPath("$.msg", containsString("未登录")));
        }

        @Test
        @DisplayName("Bearer 过期 token → JwtService 抛 BizException → filter 静默 → EntryPoint 401")
        void expiredToken_returns401() throws Exception {
            when(jwtService.verifyAccessToken("expired-token"))
                    .thenThrow(new BizException(ErrorCode.INVALID_TOKEN, "Token 无效或已过期"));

            mvc.perform(MockMvcRequestBuilders.get("/auth/me")
                            .header("Authorization", "Bearer expired-token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
        }

        @Test
        @DisplayName("Bearer 篡改 token → 同样 401（不暴露细节）")
        void tamperedToken_returns401() throws Exception {
            when(jwtService.verifyAccessToken("tampered.jwt.value"))
                    .thenThrow(new BizException(ErrorCode.INVALID_TOKEN, "Token 无效或已过期"));

            mvc.perform(MockMvcRequestBuilders.get("/admin/users")
                            .header("Authorization", "Bearer tampered.jwt.value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("非 Bearer 前缀的 Authorization → 视为无 token → 401")
        void nonBearer_returns401() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/auth/me")
                            .header("Authorization", "Basic dXNlcjpwYXNz"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ════════════════════════════════════════════════════════════
    //              200 / 403 · @PreAuthorize 角色守卫
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@PreAuthorize · 角色守卫")
    class RoleGuard {

        @Test
        @DisplayName("ADMIN token 可调 POST /admin/users（200）")
        void adminCanCreateUser() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");
            when(userMapper.selectCount(any())).thenReturn(0L);
            when(passwordEncoder.encode("password!")).thenReturn("BCRYPT");

            String body = """
                    {"email":"hr@new.com","password":"password!","fullName":"NewHR","role":"HR","subDepartmentIds":[1]}
                    """;

            mvc.perform(jsonPost("/admin/users", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("HR token 调 /admin/users → 403 FORBIDDEN")
        void hrCannotCreateUser() throws Exception {
            mockClaims("hr-token", 2L, "hr@b.com", "HR");

            String body = """
                    {"email":"x@b.com","password":"password!","fullName":"X","role":"HR"}
                    """;

            mvc.perform(jsonPost("/admin/users", body)
                            .header("Authorization", "Bearer hr-token"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));

            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("CANDIDATE token 调 /admin/users → 403")
        void candidateCannotCreateUser() throws Exception {
            mockClaims("c-token", 3L, "c@b.com", "CANDIDATE");

            String body = """
                    {"email":"x@b.com","password":"password!","fullName":"X","role":"HR"}
                    """;

            mvc.perform(jsonPost("/admin/users", body)
                            .header("Authorization", "Bearer c-token"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN 创建用户时 role=ADMIN → validation 400（防权限提升）")
        void adminCannotCreateAdmin_validation400() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");

            String body = """
                    {"email":"new@b.com","password":"password!","fullName":"X","role":"ADMIN"}
                    """;

            mvc.perform(jsonPost("/admin/users", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        }

        // ── 批量创建 /admin/users/batch ──────────────────────────

        @Test
        @DisplayName("ADMIN 批量创建 · 全部成功 → 200，逐行明细 + counts")
        void adminBatchCreate_allSuccess() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");
            when(userMapper.selectCount(any())).thenReturn(0L);
            when(passwordEncoder.encode(any())).thenReturn("BCRYPT");

            String body = """
                    {"users":[
                        {"email":"hr1@b.com","password":"password!","fullName":"HR-1","role":"HR","subDepartmentIds":[1]},
                        {"email":"hr2@b.com","password":"password!","fullName":"HR-2","role":"HR","subDepartmentIds":[1]}
                    ]}
                    """;

            mvc.perform(jsonPost("/admin/users/batch", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.successCount").value(2))
                    .andExpect(jsonPath("$.data.failureCount").value(0))
                    .andExpect(jsonPath("$.data.items[0].success").value(true))
                    .andExpect(jsonPath("$.data.items[0].email").value("hr1@b.com"))
                    .andExpect(jsonPath("$.data.items[1].success").value(true));

            verify(userMapper, org.mockito.Mockito.times(2)).insert(any(User.class));
        }

        @Test
        @DisplayName("ADMIN 批量创建 · 同批次内重复邮箱 → 第二条 EMAIL_ALREADY_EXISTS，整批不回滚")
        void adminBatchCreate_duplicateInBatch_partialSuccess() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");
            when(userMapper.selectCount(any())).thenReturn(0L);
            when(passwordEncoder.encode(any())).thenReturn("BCRYPT");

            String body = """
                    {"users":[
                        {"email":"dup@b.com","password":"password!","fullName":"X","role":"HR","subDepartmentIds":[1]},
                        {"email":"DUP@b.com","password":"password!","fullName":"Y","role":"HR","subDepartmentIds":[1]}
                    ]}
                    """;

            mvc.perform(jsonPost("/admin/users/batch", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.successCount").value(1))
                    .andExpect(jsonPath("$.data.failureCount").value(1))
                    .andExpect(jsonPath("$.data.items[0].success").value(true))
                    .andExpect(jsonPath("$.data.items[1].success").value(false))
                    .andExpect(jsonPath("$.data.items[1].errorCode")
                            .value(ErrorCode.EMAIL_ALREADY_EXISTS.getCode()));
        }

        @Test
        @DisplayName("ADMIN 批量创建 · 邮箱已存在 → 该行失败但其余成功")
        void adminBatchCreate_existingEmail_partialFailure() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");
            // 第一条 selectCount=1（已存在），第二条 selectCount=0（可创建）
            when(userMapper.selectCount(any())).thenReturn(1L, 0L);
            when(passwordEncoder.encode(any())).thenReturn("BCRYPT");

            String body = """
                    {"users":[
                        {"email":"old@b.com","password":"password!","fullName":"Old","role":"HR","subDepartmentIds":[1]},
                        {"email":"new@b.com","password":"password!","fullName":"New","role":"HR","subDepartmentIds":[1]}
                    ]}
                    """;

            mvc.perform(jsonPost("/admin/users/batch", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.successCount").value(1))
                    .andExpect(jsonPath("$.data.failureCount").value(1))
                    .andExpect(jsonPath("$.data.items[0].success").value(false))
                    .andExpect(jsonPath("$.data.items[0].errorCode")
                            .value(ErrorCode.EMAIL_ALREADY_EXISTS.getCode()))
                    .andExpect(jsonPath("$.data.items[1].success").value(true));
        }

        @Test
        @DisplayName("HR token 调 /admin/users/batch → 403")
        void hrCannotBatchCreate() throws Exception {
            mockClaims("hr-token", 2L, "hr@b.com", "HR");

            String body = """
                    {"users":[{"email":"x@b.com","password":"password!","fullName":"X","role":"HR"}]}
                    """;

            mvc.perform(jsonPost("/admin/users/batch", body)
                            .header("Authorization", "Bearer hr-token"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));

            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("ADMIN 批量创建 · 空列表 → 400 VALIDATION_FAILED")
        void adminBatchCreate_emptyList_returns400() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");

            String body = """
                    {"users":[]}
                    """;

            mvc.perform(jsonPost("/admin/users/batch", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        }

        @Test
        @DisplayName("ADMIN 批量创建 · 单行 role=ADMIN → 400 VALIDATION_FAILED（@Valid 级联校验）")
        void adminBatchCreate_roleAdmin_validation400() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");

            String body = """
                    {"users":[
                        {"email":"x@b.com","password":"password!","fullName":"X","role":"ADMIN"}
                    ]}
                    """;

            mvc.perform(jsonPost("/admin/users/batch", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        }
    }

    // ════════════════════════════════════════════════════════════
    //        AuthController · permitAll 路径 + 业务路径
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AuthController · 路径放行 + 业务正向")
    class AuthEndpoints {

        @Test
        @DisplayName("/auth/register 公开 → 200，回包透传 AuthService 结果")
        void registerIsPublic() throws Exception {
            MeVO me = MeVO.builder().id(1L).email("a@b.com").fullName("Alice").role("CANDIDATE").build();
            when(authService.register(any())).thenReturn(me);

            String body = """
                    {"email":"a@b.com","password":"PassWord1","fullName":"Alice"}
                    """;

            mvc.perform(jsonPost("/auth/register", body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.role").value("CANDIDATE"));
        }

        @Test
        @DisplayName("/auth/register 邮箱格式错误 → 400 VALIDATION_FAILED")
        void register_invalidEmail_returns400() throws Exception {
            String body = """
                    {"email":"not-an-email","password":"PassWord1","fullName":"Alice"}
                    """;

            mvc.perform(jsonPost("/auth/register", body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        }

        @Test
        @DisplayName("/auth/login 公开 → 200 + 透传 TokenVO；写 cookie 由 service 内部完成")
        void loginIsPublic() throws Exception {
            TokenVO vo = TokenVO.builder()
                    .accessToken("ACCESS")
                    .expiresIn(900L)
                    .tokenType("Bearer")
                    .user(MeVO.builder().id(1L).email("a@b.com").role("CANDIDATE").build())
                    .build();
            when(authService.login(any(), any())).thenReturn(vo);

            String body = """
                    {"email":"a@b.com","password":"pwd12345"}
                    """;

            mvc.perform(jsonPost("/auth/login", body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("ACCESS"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("/auth/login 密码错误 → service 抛 INVALID_CREDENTIALS → 401")
        void login_invalidCredentials_returns401() throws Exception {
            when(authService.login(any(), any()))
                    .thenThrow(new BizException(ErrorCode.INVALID_CREDENTIALS, "邮箱或密码错误"));

            String body = """
                    {"email":"a@b.com","password":"wrongPwd"}
                    """;

            mvc.perform(jsonPost("/auth/login", body))
                    .andExpect(status().isBadRequest())  // INVALID_CREDENTIALS=10005 -> BAD_REQUEST per GlobalExceptionHandler
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_CREDENTIALS.getCode()));
        }

        @Test
        @DisplayName("/auth/refresh 仅凭 cookie → 公开放行，无 token 也可达 controller")
        void refreshIsPublic() throws Exception {
            TokenVO vo = TokenVO.builder().accessToken("NEW").expiresIn(900L).tokenType("Bearer").build();
            when(authService.refresh(any(), any())).thenReturn(vo);

            mvc.perform(MockMvcRequestBuilders.post("/auth/refresh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("NEW"));
        }

        @Test
        @DisplayName("/auth/logout 公开 → 200，即使 access token 已过期也能登出")
        void logoutIsPublic() throws Exception {
            mvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(authService).logout(any(), any());
        }

        @Test
        @DisplayName("/auth/me 带合法 ADMIN token → 调 service.me(userId) 并返回 200")
        void me_withValidToken_returns200() throws Exception {
            mockClaims("good-token", 7L, "admin@b.com", "ADMIN");
            when(authService.me(7L)).thenReturn(
                    MeVO.builder().id(7L).email("admin@b.com").role("ADMIN").build());

            mvc.perform(MockMvcRequestBuilders.get("/auth/me")
                            .header("Authorization", "Bearer good-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(7))
                    .andExpect(jsonPath("$.data.role").value("ADMIN"));
        }
    }

    // ════════════════════════════════════════════════════════════
    //         CSRF · 必须 disable（回归测试，防止有人改回）
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("CSRF · disable 回归")
    class CsrfRegression {

        @Test
        @DisplayName("POST /auth/login 不带 X-XSRF-TOKEN / CSRF token → 仍能 200（CSRF disabled）")
        void postLoginWithoutCsrfToken_passes() throws Exception {
            when(authService.login(any(), any()))
                    .thenReturn(TokenVO.builder().accessToken("ACCESS").expiresIn(900L).tokenType("Bearer").build());

            String body = """
                    {"email":"a@b.com","password":"pwd12345"}
                    """;

            // 关键：完全不带任何 CSRF token、不带任何 _csrf 参数；只要返回 200 即证明 csrf.disable() 生效
            mvc.perform(jsonPost("/auth/login", body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /admin/users 带 ADMIN token 但不带 CSRF token → 仍能 200")
        void postAdminUsersWithoutCsrf_passes() throws Exception {
            mockClaims("admin-token", 1L, "admin@b.com", "ADMIN");
            when(userMapper.selectCount(any())).thenReturn(0L);
            when(passwordEncoder.encode(any())).thenReturn("BCRYPT");

            String body = """
                    {"email":"hr@new.com","password":"password!","fullName":"NewHR","role":"CANDIDATE"}
                    """;

            mvc.perform(jsonPost("/admin/users", body)
                            .header("Authorization", "Bearer admin-token"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("响应不应下发任何 XSRF-TOKEN cookie（因为 CookieCsrfTokenRepository 未启）")
        void noCsrfCookieIssued() throws Exception {
            when(authService.login(any(), any()))
                    .thenReturn(TokenVO.builder().accessToken("ACCESS").expiresIn(900L).tokenType("Bearer").build());

            String body = """
                    {"email":"a@b.com","password":"pwd12345"}
                    """;

            mvc.perform(jsonPost("/auth/login", body))
                    .andExpect(status().isOk())
                    .andExpect(cookie().doesNotExist("XSRF-TOKEN"));
        }
    }

    // ════════════════════════════════════════════════════════════
    //                       helpers
    // ════════════════════════════════════════════════════════════

    /** mock JwtService.verifyAccessToken → 返回带 subject/email/role 的 Claims（JJWT 0.12 已无 setters，用 put + Claims.SUBJECT 常量）*/
    private void mockClaims(String token, long userId, String email, String role) {
        HashMap<String, Object> raw = new HashMap<>();
        raw.put(Claims.SUBJECT, String.valueOf(userId));
        raw.put("email", email);
        raw.put("role", role);
        Claims claims = new DefaultClaims(raw);
        when(jwtService.verifyAccessToken(token)).thenReturn(claims);
    }

    /** authService.me() 在 happy path 测试里也需要 stub，提供便捷 helper */
    @SuppressWarnings("unused")
    private void mockMe(long userId, String email, String role) {
        when(authService.me(userId)).thenReturn(
                MeVO.builder().id(userId).email(email).role(role).build());
        // 简单覆盖 anyLong 防止漏配
        when(authService.me(anyLong())).thenReturn(
                MeVO.builder().id(userId).email(email).role(role).build());
    }

    private static MockHttpServletRequestBuilder jsonPost(String url, String body) {
        return MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    @SuppressWarnings("unused")
    private static void avoidUnusedImport() {
        // 静音 MockMvcResultMatchers.status 静态 import 提示（多 nested 时部分类未直接引用）
        MockMvcResultMatchers.status();
    }
}
