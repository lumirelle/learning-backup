package com.ats.application;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.ApplicationStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 8 态招聘漏斗状态机纯单测：
 * <ul>
 *   <li>12 条合法边（6 forward + 6 reject 横切）</li>
 *   <li>典型非法（回退、跳级、终态再变）</li>
 *   <li>自环全 8 态禁止</li>
 *   <li>终态保护：HIRED / REJECTED 无任何下一步</li>
 *   <li>nextStages / isTerminal / null 边界</li>
 * </ul>
 * 0 Spring / 0 DB 依赖，全跑 &lt; 100ms。
 */
@DisplayName("ApplicationStageMachine · 8 态状态机")
class ApplicationStageMachineTest {

    // ─────────────────── 合法流转：6 条 forward + 6 条 reject ───────────────────

    @Nested
    @DisplayName("合法流转 (12 条边)")
    class Legal {

        @ParameterizedTest(name = "[{index}] {0} → {1} 合法")
        @CsvSource({
                // forward
                "APPLIED,         SCREENING_PASS",
                "SCREENING_PASS,  PHONE_INTERVIEW",
                "PHONE_INTERVIEW, TECH_INTERVIEW",
                "TECH_INTERVIEW,  HR_INTERVIEW",
                "HR_INTERVIEW,    OFFER",
                "OFFER,           HIRED",
                // 任意非终态 → REJECTED
                "APPLIED,         REJECTED",
                "SCREENING_PASS,  REJECTED",
                "PHONE_INTERVIEW, REJECTED",
                "TECH_INTERVIEW,  REJECTED",
                "HR_INTERVIEW,    REJECTED",
                "OFFER,           REJECTED",
        })
        void canTransition_returnsTrue(ApplicationStage from, ApplicationStage to) {
            assertThat(ApplicationStageMachine.canTransition(from, to)).isTrue();
        }

        @ParameterizedTest(name = "[{index}] requireTransition {0} → {1} 不抛")
        @CsvSource({
                "APPLIED,         SCREENING_PASS",
                "HR_INTERVIEW,    OFFER",
                "OFFER,           HIRED",
                "PHONE_INTERVIEW, REJECTED",
        })
        void requireTransition_doesNotThrow(ApplicationStage from, ApplicationStage to) {
            ApplicationStageMachine.requireTransition(from, to);
        }
    }

    // ─────────────────── 非法 ───────────────────

    @Nested
    @DisplayName("非法流转 · 抛 BizException(ILLEGAL_TRANSITION)")
    class Illegal {

        @ParameterizedTest(name = "[{index}] {0} → {1} 非法")
        @CsvSource({
                // 跳级（不允许跨阶段）
                "APPLIED,         PHONE_INTERVIEW",
                "APPLIED,         OFFER",
                "SCREENING_PASS,  TECH_INTERVIEW",
                "SCREENING_PASS,  HIRED",
                "PHONE_INTERVIEW, OFFER",
                // 回退
                "SCREENING_PASS,  APPLIED",
                "PHONE_INTERVIEW, APPLIED",
                "TECH_INTERVIEW,  PHONE_INTERVIEW",
                "HR_INTERVIEW,    TECH_INTERVIEW",
                "OFFER,           HR_INTERVIEW",
                // OFFER 不能直接 HIRED 之外的地方（除已覆盖的 REJECTED）
                "OFFER,           APPLIED",
        })
        void canTransition_returnsFalse(ApplicationStage from, ApplicationStage to) {
            assertThat(ApplicationStageMachine.canTransition(from, to)).isFalse();
        }

        @ParameterizedTest(name = "[{index}] 自环 {0} → {0} 不允许")
        @EnumSource(ApplicationStage.class)
        void selfLoop_isIllegal(ApplicationStage s) {
            assertThat(ApplicationStageMachine.canTransition(s, s)).isFalse();
        }

        @Test
        @DisplayName("requireTransition 非法 → 抛 ILLEGAL_TRANSITION 并附带 from→to 与允许列表")
        void requireTransition_throwsWithContext() {
            assertThatThrownBy(() -> ApplicationStageMachine.requireTransition(
                    ApplicationStage.SCREENING_PASS, ApplicationStage.HIRED))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("SCREENING_PASS")
                    .hasMessageContaining("HIRED")
                    .hasMessageContaining("PHONE_INTERVIEW") // 允许列表里有这一条
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ILLEGAL_TRANSITION));
        }
    }

    // ─────────────────── 终态保护 ───────────────────

    @Nested
    @DisplayName("终态保护 · HIRED / REJECTED")
    class TerminalProtection {

        @ParameterizedTest(name = "[{index}] HIRED → {0} 一律抛 APPLICATION_TERMINATED")
        @EnumSource(ApplicationStage.class)
        void hired_cannotLeaveTerminal(ApplicationStage to) {
            assertThatThrownBy(() -> ApplicationStageMachine.requireTransition(ApplicationStage.HIRED, to))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.APPLICATION_TERMINATED));
        }

        @ParameterizedTest(name = "[{index}] REJECTED → {0} 一律抛 APPLICATION_TERMINATED")
        @EnumSource(ApplicationStage.class)
        void rejected_cannotLeaveTerminal(ApplicationStage to) {
            assertThatThrownBy(() -> ApplicationStageMachine.requireTransition(ApplicationStage.REJECTED, to))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> assertThat(((BizException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.APPLICATION_TERMINATED));
        }

        @Test
        @DisplayName("isTerminal: HIRED & REJECTED true，其余 false")
        void isTerminal() {
            assertThat(ApplicationStageMachine.isTerminal(ApplicationStage.HIRED)).isTrue();
            assertThat(ApplicationStageMachine.isTerminal(ApplicationStage.REJECTED)).isTrue();
            assertThat(ApplicationStageMachine.isTerminal(ApplicationStage.APPLIED)).isFalse();
            assertThat(ApplicationStageMachine.isTerminal(ApplicationStage.OFFER)).isFalse();
            assertThat(ApplicationStageMachine.isTerminal(null)).isFalse();
        }
    }

    // ─────────────────── 边界 ───────────────────

    @Nested
    @DisplayName("边界 · null / nextStages")
    class Edge {

        @Test
        @DisplayName("from=null / to=null → canTransition false")
        void nullCheck() {
            assertThat(ApplicationStageMachine.canTransition(null, ApplicationStage.SCREENING_PASS)).isFalse();
            assertThat(ApplicationStageMachine.canTransition(ApplicationStage.APPLIED, null)).isFalse();
            assertThat(ApplicationStageMachine.canTransition(null, null)).isFalse();
        }

        @Test
        @DisplayName("nextStages(APPLIED) = {SCREENING_PASS, REJECTED}")
        void nextStages_applied() {
            assertThat(ApplicationStageMachine.nextStages(ApplicationStage.APPLIED))
                    .containsExactlyInAnyOrder(ApplicationStage.SCREENING_PASS, ApplicationStage.REJECTED);
        }

        @Test
        @DisplayName("nextStages(OFFER) = {HIRED, REJECTED}")
        void nextStages_offer() {
            assertThat(ApplicationStageMachine.nextStages(ApplicationStage.OFFER))
                    .containsExactlyInAnyOrder(ApplicationStage.HIRED, ApplicationStage.REJECTED);
        }

        @Test
        @DisplayName("nextStages(HIRED) / nextStages(REJECTED) → empty")
        void nextStages_terminals() {
            assertThat(ApplicationStageMachine.nextStages(ApplicationStage.HIRED)).isEmpty();
            assertThat(ApplicationStageMachine.nextStages(ApplicationStage.REJECTED)).isEmpty();
        }

        @Test
        @DisplayName("nextStages(null) = empty")
        void nextStages_null() {
            assertThat(ApplicationStageMachine.nextStages(null)).isEmpty();
        }
    }
}
