package com.ats.entity;

/**
 * 面试结论（与 PG enum {@code interview_conclusion} 对齐）。
 *
 * <ul>
 *   <li>{@link #PASS} 通过：候选人 OK，建议进入下一阶段</li>
 *   <li>{@link #REJECT} 拒绝：明确不通过</li>
 *   <li>{@link #HOLD} 待定：需补充材料 / 再面 / 多面试官对齐</li>
 * </ul>
 *
 * <p><strong>注意</strong>：和 application stage 的 REJECTED 不是一回事 ——
 * 面试结论只是「这一轮的判定」，是否真的拒绝进入终态由 HR 在看板上决策。</p>
 */
public enum InterviewConclusion {
    PASS,
    REJECT,
    HOLD
}
