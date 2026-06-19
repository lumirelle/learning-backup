package com.ats.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunnelVO {
    /** 8 个 stage 固定顺序输出（即使某 stage count=0 也要返回） */
    private List<FunnelItemVO> items;
    /** 投递总数（= 所有 stage 之和） */
    private long total;
    /** 漏斗最大 stage count（用于前端按比例渲染条形图，避免再算） */
    private long max;
}
