package com.ats.job;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.Job;
import com.ats.entity.JobLevel;
import com.ats.entity.JobStatus;
import com.ats.entity.JobWorkType;
import com.ats.entity.User;
import com.ats.job.dto.JobCreateReq;
import com.ats.job.dto.JobDetailVO;
import com.ats.job.dto.JobTransitionReq;
import com.ats.job.dto.JobUpdateReq;
import com.ats.entity.SubDepartment;
import com.ats.repository.JobMapper;
import com.ats.repository.JobTagMapper;
import com.ats.repository.SubDepartmentMapper;
import com.ats.repository.TagMapper;
import com.ats.repository.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JobService 纯 Mockito 单测，覆盖：
 *  · create   ：happy / salary 反向 / 不存在的 tag
 *  · update   ：happy / 非 owner 拒绝 / 标签全量替换
 *  · transition：合法 / 非法 / 非 owner
 *  · softDelete：HR 拒绝 / Admin 通过 / 不存在
 *  · getDetail ：候选人看 DRAFT 被拒 / Admin 看全 / view_count +1
 *
 * SecurityContext 通过 setAuth(...) helper 注入。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JobService · 业务分支覆盖")
class JobServiceTest {

    @Mock JobMapper jobMapper;
    @Mock TagMapper tagMapper;
    @Mock JobTagMapper jobTagMapper;
    @Mock UserMapper userMapper;
    @Mock SubDepartmentMapper subDepartmentMapper;
    @Mock HrJobScopeService hrJobScopeService;

    @InjectMocks JobService jobService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        when(hrJobScopeService.canManageJob(any())).thenAnswer(inv -> {
            Job job = inv.getArgument(0);
            if (job == null) return false;
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
            return java.util.Objects.equals(Long.parseLong(auth.getName()), job.getCreatedBy());
        });
        // 默认 stub：subDept / user / tagRow batch 查回空，简化 toVO 路径
        when(subDepartmentMapper.selectExpandedByIds(anyList())).thenReturn(Collections.emptyList());
        when(userMapper.selectByIds(anyList())).thenReturn(Collections.emptyList());
        when(tagMapper.findTagsByJobIds(anyList())).thenReturn(Collections.emptyList());
        // M6: validateSubDepartmentExists 走 subDepartmentMapper.selectById；create/update 路径默认放行
        when(subDepartmentMapper.selectById(anyLong())).thenReturn(new SubDepartment());
        // 注意：tagMapper.selectCount 不设默认 → 各 case 按需 stub（避免与 validateTagIds 隐式冲突）
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ═════════════════════════════ create ═════════════════════════════

    @Nested
    @DisplayName("create · 创建岗位")
    class Create {

        @Test
        @DisplayName("happy path：HR 创建 → status=DRAFT、createdBy=currentUser、关联 tags")
        void happyPath() {
            setAuth(10L, "HR");
            JobCreateReq req = baseCreateReq();
            req.setTagIds(List.of(1L, 2L));

            // tagMapper.selectCount 必须返回与 tagIds.size 一致，否则 validateTagIds 抛 TAG_NOT_FOUND
            when(tagMapper.selectCount(any())).thenReturn(2L);
            // insert 时往 entity 写 id
            doAnswerSetId(jobMapper, 100L);
            when(jobMapper.selectById(100L)).thenReturn(loadedJob(100L, 10L, JobStatus.DRAFT));

            JobDetailVO vo = jobService.create(req);

            assertThat(vo.getStatus()).isEqualTo(JobStatus.DRAFT);
            verify(jobMapper).insert(any(Job.class));
            verify(jobTagMapper).batchInsert(eq(100L), eq(List.of(1L, 2L)));
        }

        @Test
        @DisplayName("salary_min > salary_max → JOB_SALARY_RANGE_INVALID")
        void invalidSalaryRange() {
            setAuth(10L, "HR");
            JobCreateReq req = baseCreateReq();
            req.setSalaryMin(50000);
            req.setSalaryMax(30000);

            assertThatThrownBy(() -> jobService.create(req))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_SALARY_RANGE_INVALID));

            verify(jobMapper, never()).insert(any(Job.class));
        }

        @Test
        @DisplayName("tagId 不存在 → TAG_NOT_FOUND")
        void invalidTagId() {
            setAuth(10L, "HR");
            JobCreateReq req = baseCreateReq();
            req.setTagIds(List.of(1L, 999L));
            when(tagMapper.selectCount(any())).thenReturn(1L); // 只有 1 个存在

            assertThatThrownBy(() -> jobService.create(req))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.TAG_NOT_FOUND));
        }

        @Test
        @DisplayName("未登录 → UNAUTHORIZED")
        void anonymous_rejected() {
            // 不调 setAuth
            assertThatThrownBy(() -> jobService.create(baseCreateReq()))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.UNAUTHORIZED));
        }
    }

    // ═════════════════════════════ update ═════════════════════════════

    @Nested
    @DisplayName("update · 编辑岗位")
    class Update {

        @Test
        @DisplayName("owner 改 title → 成功")
        void ownerUpdate_ok() {
            setAuth(10L, "HR");
            Job old = loadedJob(1L, 10L, JobStatus.PUBLISHED);
            when(jobMapper.selectById(1L)).thenReturn(old);

            JobUpdateReq req = new JobUpdateReq();
            req.setTitle("new title");

            jobService.update(1L, req);

            verify(jobMapper).updateById(any(Job.class));
        }

        @Test
        @DisplayName("非 owner（HR 改他人岗位）→ JOB_ACCESS_DENIED")
        void nonOwner_denied() {
            setAuth(20L, "HR"); // current=20，但岗位 createdBy=10
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> jobService.update(1L, new JobUpdateReq()))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_ACCESS_DENIED));

            verify(jobMapper, never()).updateById(any(Job.class));
        }

        @Test
        @DisplayName("Admin 改任意人岗位 → 成功（覆盖 ownership）")
        void adminCanUpdateAny() {
            setAuth(1L, "ADMIN");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 999L, JobStatus.PUBLISHED));

            jobService.update(1L, new JobUpdateReq());

            verify(jobMapper).updateById(any(Job.class));
        }

        @Test
        @DisplayName("tagIds=[] → 清空标签关联（先删后空插）")
        void emptyTagIds_clears() {
            setAuth(10L, "HR");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.DRAFT));

            JobUpdateReq req = new JobUpdateReq();
            req.setTagIds(Collections.emptyList());

            jobService.update(1L, req);

            verify(jobTagMapper).deleteByJobId(1L);
            verify(jobTagMapper, never()).batchInsert(anyLong(), anyList());
        }

        @Test
        @DisplayName("岗位不存在 → JOB_NOT_FOUND")
        void notFound() {
            setAuth(10L, "HR");
            when(jobMapper.selectById(404L)).thenReturn(null);

            assertThatThrownBy(() -> jobService.update(404L, new JobUpdateReq()))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_NOT_FOUND));
        }
    }

    // ═════════════════════════════ transition ═════════════════════════════

    @Nested
    @DisplayName("transition · 状态机推进")
    class Transition {

        @Test
        @DisplayName("DRAFT → PUBLISHED：合法 + 自动写 publishedAt")
        void draftToPublished_setsPublishedAt() {
            setAuth(10L, "HR");
            Job j = loadedJob(1L, 10L, JobStatus.DRAFT);
            j.setPublishedAt(null);
            when(jobMapper.selectById(1L)).thenReturn(j);

            jobService.transition(1L, JobStatus.PUBLISHED);

            verify(jobMapper).updateById(any(Job.class));
            assertThat(j.getStatus()).isEqualTo("PUBLISHED");
            assertThat(j.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("PUBLISHED → CLOSED：合法 + 自动写 closedAt")
        void publishedToClosed_setsClosedAt() {
            setAuth(10L, "HR");
            Job j = loadedJob(1L, 10L, JobStatus.PUBLISHED);
            j.setClosedAt(null);
            when(jobMapper.selectById(1L)).thenReturn(j);

            jobService.transition(1L, JobStatus.CLOSED);

            assertThat(j.getStatus()).isEqualTo("CLOSED");
            assertThat(j.getClosedAt()).isNotNull();
        }

        @Test
        @DisplayName("PUBLISHED → DRAFT：非法 → ILLEGAL_TRANSITION")
        void illegalTransition() {
            setAuth(10L, "HR");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            assertThatThrownBy(() -> jobService.transition(1L, JobStatus.DRAFT))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ILLEGAL_TRANSITION));

            verify(jobMapper, never()).updateById(any(Job.class));
        }

        @Test
        @DisplayName("非 owner 推流转 → JOB_ACCESS_DENIED")
        void nonOwner_denied() {
            setAuth(20L, "HR");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.DRAFT));

            assertThatThrownBy(() -> jobService.transition(1L, JobStatus.PUBLISHED))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_ACCESS_DENIED));
        }
    }

    // ═════════════════════════════ softDelete ═════════════════════════════

    @Nested
    @DisplayName("softDelete · 软删")
    class SoftDelete {

        @Test
        @DisplayName("HR 调用 → JOB_ACCESS_DENIED（只允许 Admin）")
        void hrCannotDelete() {
            setAuth(10L, "HR");

            assertThatThrownBy(() -> jobService.softDelete(1L))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_ACCESS_DENIED));

            verify(jobMapper, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Admin 调用 → 走 jobMapper.deleteById")
        void adminCanDelete() {
            setAuth(1L, "ADMIN");
            when(jobMapper.selectById(5L)).thenReturn(loadedJob(5L, 10L, JobStatus.CLOSED));

            jobService.softDelete(5L);

            verify(jobMapper).deleteById(5L);
        }
    }

    // ═════════════════════════════ getDetail ═════════════════════════════

    @Nested
    @DisplayName("getDetail · 详情可见性")
    class GetDetail {

        @Test
        @DisplayName("候选人看 DRAFT → JOB_NOT_PUBLISHED")
        void candidateSeeDraft_denied() {
            setAuth(99L, "CANDIDATE");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.DRAFT));

            assertThatThrownBy(() -> jobService.getDetail(1L))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JOB_NOT_PUBLISHED));
        }

        @Test
        @DisplayName("候选人看 PUBLISHED → 200 + view_count +1")
        void candidateSeePublished_incrementsView() {
            setAuth(99L, "CANDIDATE");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.PUBLISHED));

            JobDetailVO vo = jobService.getDetail(1L);

            assertThat(vo.getStatus()).isEqualTo(JobStatus.PUBLISHED);
            verify(jobMapper).incrementViewCount(1L);
            assertThat(vo.getAllowedTransitions()).isNull(); // 候选人无管理动作
        }

        @Test
        @DisplayName("HR 看自己的 DRAFT → 200 + 不计 view + 返回 allowedTransitions")
        void hrSeeOwnDraft_noViewIncrement() {
            setAuth(10L, "HR");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.DRAFT));

            JobDetailVO vo = jobService.getDetail(1L);

            assertThat(vo.getStatus()).isEqualTo(JobStatus.DRAFT);
            verify(jobMapper, never()).incrementViewCount(anyLong());
            assertThat(vo.getAllowedTransitions())
                    .containsExactlyInAnyOrder(JobStatus.PUBLISHED, JobStatus.ARCHIVED);
        }

        @Test
        @DisplayName("Admin 看 ARCHIVED → 200 + allowedTransitions=[DRAFT]")
        void adminSeeArchived() {
            setAuth(1L, "ADMIN");
            when(jobMapper.selectById(1L)).thenReturn(loadedJob(1L, 10L, JobStatus.ARCHIVED));

            JobDetailVO vo = jobService.getDetail(1L);

            assertThat(vo.getAllowedTransitions()).containsExactly(JobStatus.DRAFT);
        }
    }

    // ═════════════════════════════ helpers ═════════════════════════════

    private static void setAuth(long userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static JobCreateReq baseCreateReq() {
        JobCreateReq req = new JobCreateReq();
        req.setTitle("Test Engineer");
        req.setDescription("desc");
        // M6: location 字段已从 jobs 表迁出，改通过 subDepartment 关联；测试用一个 stub id
        req.setSubDepartmentId(1L);
        req.setWorkType(JobWorkType.FULL_TIME);
        req.setLevel(JobLevel.MID);
        req.setSalaryMin(20000);
        req.setSalaryMax(35000);
        req.setHeadcount((short) 1);
        return req;
    }

    private static Job loadedJob(long id, long createdBy, JobStatus status) {
        Job j = new Job();
        j.setId(id);
        j.setCreatedBy(createdBy);
        j.setTitle("title-" + id);
        j.setDescription("desc-" + id);
        j.setSubDepartmentId(1L);
        j.setWorkType(JobWorkType.FULL_TIME.name());
        j.setLevel(JobLevel.MID.name());
        j.setSalaryMin(20000);
        j.setSalaryMax(35000);
        j.setHeadcount((short) 1);
        j.setStatus(status.name());
        j.setViewCount(0);
        j.setCreatedAt(OffsetDateTime.now());
        j.setUpdatedAt(OffsetDateTime.now());
        return j;
    }

    /** 让 jobMapper.insert(job) 把 generated id 写回 entity（模拟 KeyHolder 行为）。 */
    private static void doAnswerSetId(JobMapper mapper, long id) {
        when(mapper.insert(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(id);
            return 1;
        });
    }

    @SuppressWarnings("unused")
    private static void avoidUnusedImport() {
        // 静音未使用 import 提示
        new JobTransitionReq();
        User u = new User();
        u.toString();
    }
}
