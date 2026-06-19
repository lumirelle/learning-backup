package com.ats.application.dto;

import com.ats.entity.ApplicationStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 看板单列（一个 stage）：含计数 + 该列前 N 个投递的概览。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnVO {
    private ApplicationStage stage;
    private long count;
    private List<ApplicationListItemVO> items;
    /** 该列是否还有未加载的投递（count &gt; items.size()） */
    private boolean hasMore;
}
