package com.ats.application;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.ApplicationStage;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 候选人投递 8 态状态机。
 *
 * <pre>
 *   APPLIED        ──pass─────▶ SCREENING_PASS    ──reject──▶ REJECTED
 *   SCREENING_PASS ──schedule──▶ PHONE_INTERVIEW   ──reject──▶ REJECTED
 *   PHONE_INTERVIEW──pass──────▶ TECH_INTERVIEW    ──reject──▶ REJECTED
 *   TECH_INTERVIEW ──pass──────▶ HR_INTERVIEW      ──reject──▶ REJECTED
 *   HR_INTERVIEW   ──offer─────▶ OFFER             ──reject──▶ REJECTED
 *   OFFER          ──accept────▶ HIRED             ──reject──▶ REJECTED
 *
 *   HIRED · REJECTED → 终态，无任何下一步
 * </pre>
 *
 * 任何 from→to 不在合法图内（含自环、回退）即非法。
 * REJECTED 是「死亡线」：任意非终态都可流转到它，但终态不能离开终态。
 */
public final class ApplicationStageMachine {

    private static final Map<ApplicationStage, Set<ApplicationStage>> ALLOWED;

    /** 终态集合：HIRED / REJECTED。任何状态进入此集合即冻结。 */
    public static final Set<ApplicationStage> TERMINAL_STAGES =
            Collections.unmodifiableSet(EnumSet.of(ApplicationStage.HIRED, ApplicationStage.REJECTED));

    static {
        Map<ApplicationStage, Set<ApplicationStage>> map = new EnumMap<>(ApplicationStage.class);
        map.put(ApplicationStage.APPLIED,
                EnumSet.of(ApplicationStage.SCREENING_PASS, ApplicationStage.REJECTED));
        map.put(ApplicationStage.SCREENING_PASS,
                EnumSet.of(ApplicationStage.PHONE_INTERVIEW, ApplicationStage.REJECTED));
        map.put(ApplicationStage.PHONE_INTERVIEW,
                EnumSet.of(ApplicationStage.TECH_INTERVIEW, ApplicationStage.REJECTED));
        map.put(ApplicationStage.TECH_INTERVIEW,
                EnumSet.of(ApplicationStage.HR_INTERVIEW, ApplicationStage.REJECTED));
        map.put(ApplicationStage.HR_INTERVIEW,
                EnumSet.of(ApplicationStage.OFFER, ApplicationStage.REJECTED));
        map.put(ApplicationStage.OFFER,
                EnumSet.of(ApplicationStage.HIRED, ApplicationStage.REJECTED));
        map.put(ApplicationStage.HIRED, EnumSet.noneOf(ApplicationStage.class));
        map.put(ApplicationStage.REJECTED, EnumSet.noneOf(ApplicationStage.class));
        ALLOWED = Collections.unmodifiableMap(map);
    }

    private ApplicationStageMachine() {}

    public static boolean canTransition(ApplicationStage from, ApplicationStage to) {
        if (from == null || to == null) return false;
        if (from == to) return false;
        return ALLOWED.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    /**
     * 强校验流转合法性，非法则抛 {@link ErrorCode#ILLEGAL_TRANSITION}，
     * 错误消息附带 from→to 上下文，方便前端展示。
     */
    public static void requireTransition(ApplicationStage from, ApplicationStage to) {
        if (isTerminal(from)) {
            throw new BizException(ErrorCode.APPLICATION_TERMINATED,
                    "投递已处于终态：" + from + "，不可再变更");
        }
        if (!canTransition(from, to)) {
            throw new BizException(ErrorCode.ILLEGAL_TRANSITION,
                    "非法投递阶段流转：" + from + " → " + to + "。允许的下一步：" + nextStages(from));
        }
    }

    /** 给定当前阶段，返回所有合法的下一阶段集合（前端用来动态显示流转选项）。 */
    public static Set<ApplicationStage> nextStages(ApplicationStage from) {
        if (from == null) return Collections.emptySet();
        return ALLOWED.getOrDefault(from, Collections.emptySet());
    }

    /** 是否为终态（HIRED / REJECTED）。 */
    public static boolean isTerminal(ApplicationStage stage) {
        return stage != null && TERMINAL_STAGES.contains(stage);
    }
}
