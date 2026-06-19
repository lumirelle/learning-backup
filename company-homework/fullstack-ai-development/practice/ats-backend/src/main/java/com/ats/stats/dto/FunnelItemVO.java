package com.ats.stats.dto;

import com.ats.entity.ApplicationStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunnelItemVO {
    private ApplicationStage stage;
    /** 该 stage 当前在册的 application 数（含终态） */
    private long count;
}
