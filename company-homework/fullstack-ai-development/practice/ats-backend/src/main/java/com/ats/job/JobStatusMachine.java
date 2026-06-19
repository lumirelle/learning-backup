package com.ats.job;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.JobStatus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 岗位 5 态状态机。8 条合法流转边：
 *
 * <pre>
 *   DRAFT     ─┬─ publish ─▶ PUBLISHED
 *              └─ archive ─▶ ARCHIVED
 *
 *   PUBLISHED ─┬─ pause   ─▶ PAUSED
 *              └─ close   ─▶ CLOSED
 *
 *   PAUSED    ─┬─ publish ─▶ PUBLISHED   （恢复招聘）
 *              └─ close   ─▶ CLOSED      （直接关闭）
 *
 *   CLOSED    ─── archive ─▶ ARCHIVED
 *
 *   ARCHIVED  ─── restore ─▶ DRAFT       （复用旧岗位）
 * </pre>
 *
 * 任何其它 from→to 组合都判非法（包括自环：publish→publish 等）。
 */
public final class JobStatusMachine {

    private static final Map<JobStatus, Set<JobStatus>> ALLOWED;

    static {
        Map<JobStatus, Set<JobStatus>> map = new EnumMap<>(JobStatus.class);
        map.put(JobStatus.DRAFT,     EnumSet.of(JobStatus.PUBLISHED, JobStatus.ARCHIVED));
        map.put(JobStatus.PUBLISHED, EnumSet.of(JobStatus.PAUSED, JobStatus.CLOSED));
        map.put(JobStatus.PAUSED,    EnumSet.of(JobStatus.PUBLISHED, JobStatus.CLOSED));
        map.put(JobStatus.CLOSED,    EnumSet.of(JobStatus.ARCHIVED));
        map.put(JobStatus.ARCHIVED,  EnumSet.of(JobStatus.DRAFT));
        ALLOWED = Collections.unmodifiableMap(map);
    }

    private JobStatusMachine() {}

    public static boolean canTransition(JobStatus from, JobStatus to) {
        if (from == null || to == null) return false;
        if (from == to) return false;
        return ALLOWED.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    /**
     * 强校验流转合法性，非法则抛 {@link BizException#ILLEGAL_TRANSITION}，
     * 错误消息附带 from→to 上下文，方便前端展示。
     */
    public static void requireTransition(JobStatus from, JobStatus to) {
        if (!canTransition(from, to)) {
            throw new BizException(
                    ErrorCode.ILLEGAL_TRANSITION,
                    "非法状态流转：" + from + " → " + to + "。允许的下一步：" + nextStates(from));
        }
    }

    /** 给定当前状态，返回所有合法的下一状态集合（前端用来动态显示「切换为」下拉选项）。 */
    public static Set<JobStatus> nextStates(JobStatus from) {
        if (from == null) return Collections.emptySet();
        return ALLOWED.getOrDefault(from, Collections.emptySet());
    }
}
