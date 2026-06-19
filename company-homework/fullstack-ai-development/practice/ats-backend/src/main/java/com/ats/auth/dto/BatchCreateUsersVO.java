package com.ats.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 批量创建用户结果汇总。 */
@Data
@Builder
public class BatchCreateUsersVO {

    /** 成功条数 */
    private int successCount;

    /** 失败条数 */
    private int failureCount;

    /** 逐行明细，与请求顺序一一对应 */
    private List<BatchCreateItemVO> items;
}
