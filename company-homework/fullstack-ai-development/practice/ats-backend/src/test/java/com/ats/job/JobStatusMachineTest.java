package com.ats.job;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.JobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 状态机纯单测：8 条合法边 + 全矩阵非法 + nullCheck + nextStates。
 * 无 Spring / DB 依赖，&lt; 100ms 全部跑完。
 */
@DisplayName("JobStatusMachine · 5 态状态机")
class JobStatusMachineTest {

    // ─────────────────── 合法流转：8 条边 ───────────────────

    @Nested
    @DisplayName("合法流转 (8 条边)")
    class Legal {

        @ParameterizedTest(name = "[{index}] {0} → {1} 合法")
        @CsvSource({
                "DRAFT,     PUBLISHED",
                "DRAFT,     ARCHIVED",
                "PUBLISHED, PAUSED",
                "PUBLISHED, CLOSED",
                "PAUSED,    PUBLISHED",
                "PAUSED,    CLOSED",
                "CLOSED,    ARCHIVED",
                "ARCHIVED,  DRAFT",
        })
        void canTransition_returnsTrue(JobStatus from, JobStatus to) {
            assertThat(JobStatusMachine.canTransition(from, to)).isTrue();
        }

        @ParameterizedTest(name = "[{index}] requireTransition {0} → {1} 不抛异常")
        @CsvSource({
                "DRAFT,     PUBLISHED",
                "PUBLISHED, CLOSED",
                "ARCHIVED,  DRAFT",
        })
        void requireTransition_doesNotThrow(JobStatus from, JobStatus to) {
            JobStatusMachine.requireTransition(from, to);
        }
    }

    // ─────────────────── 非法流转：典型反例 + 自环 ───────────────────

    @Nested
    @DisplayName("非法流转 · 抛 BizException(ILLEGAL_TRANSITION)")
    class Illegal {

        @ParameterizedTest(name = "[{index}] {0} → {1} 非法")
        @CsvSource({
                "PUBLISHED, DRAFT",       // 已发布不能回退草稿
                "DRAFT,     PAUSED",      // 草稿不能直接暂停
                "DRAFT,     CLOSED",      // 草稿不能直接关闭
                "PUBLISHED, ARCHIVED",    // 发布中不能直接归档（必须先 CLOSED）
                "PAUSED,    DRAFT",       // 暂停不能回退草稿
                "PAUSED,    ARCHIVED",    // 暂停不能直接归档
                "CLOSED,    PUBLISHED",   // 关闭不能恢复发布（必须先 ARCHIVED → DRAFT）
                "CLOSED,    PAUSED",
                "CLOSED,    DRAFT",
                "ARCHIVED,  PUBLISHED",
                "ARCHIVED,  PAUSED",
                "ARCHIVED,  CLOSED",
        })
        void canTransition_returnsFalse(JobStatus from, JobStatus to) {
            assertThat(JobStatusMachine.canTransition(from, to)).isFalse();
        }

        @ParameterizedTest(name = "[{index}] 自环 {0} → {0} 不允许")
        @EnumSource(JobStatus.class)
        void selfLoop_isIllegal(JobStatus s) {
            assertThat(JobStatusMachine.canTransition(s, s)).isFalse();
        }

        @Test
        @DisplayName("requireTransition 非法 → 抛 BizException(20002) 并附带 from→to 上下文")
        void requireTransition_throwsWithContext() {
            assertThatThrownBy(() -> JobStatusMachine.requireTransition(JobStatus.PUBLISHED, JobStatus.DRAFT))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("PUBLISHED")
                    .hasMessageContaining("DRAFT")
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ILLEGAL_TRANSITION));
        }
    }

    // ─────────────────── 边界 ───────────────────

    @Nested
    @DisplayName("边界 · null / nextStates")
    class Edge {

        @Test
        @DisplayName("from=null → false")
        void nullFrom_returnsFalse() {
            assertThat(JobStatusMachine.canTransition(null, JobStatus.PUBLISHED)).isFalse();
        }

        @Test
        @DisplayName("to=null → false")
        void nullTo_returnsFalse() {
            assertThat(JobStatusMachine.canTransition(JobStatus.DRAFT, null)).isFalse();
        }

        @Test
        @DisplayName("nextStates(DRAFT) = {PUBLISHED, ARCHIVED}")
        void nextStates_draft() {
            assertThat(JobStatusMachine.nextStates(JobStatus.DRAFT))
                    .containsExactlyInAnyOrder(JobStatus.PUBLISHED, JobStatus.ARCHIVED);
        }

        @Test
        @DisplayName("nextStates(PUBLISHED) = {PAUSED, CLOSED}")
        void nextStates_published() {
            assertThat(JobStatusMachine.nextStates(JobStatus.PUBLISHED))
                    .containsExactlyInAnyOrder(JobStatus.PAUSED, JobStatus.CLOSED);
        }

        @Test
        @DisplayName("nextStates(ARCHIVED) = {DRAFT}")
        void nextStates_archived() {
            assertThat(JobStatusMachine.nextStates(JobStatus.ARCHIVED))
                    .containsExactly(JobStatus.DRAFT);
        }

        @Test
        @DisplayName("nextStates(null) = empty")
        void nextStates_null() {
            assertThat(JobStatusMachine.nextStates(null)).isEmpty();
        }
    }
}
