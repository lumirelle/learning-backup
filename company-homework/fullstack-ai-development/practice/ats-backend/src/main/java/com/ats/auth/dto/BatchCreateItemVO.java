package com.ats.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 批量创建用户结果中的单行视图。
 * - success=true：返回 userId、email、role
 * - success=false：返回 errorCode、errorMsg，userId 为 null
 */
@Data
@Builder
public class BatchCreateItemVO {

    /** 在请求 list 中的索引（0-based），便于前端按行高亮 */
    private Integer rowIndex;

    private String email;

    private boolean success;

    private Long userId;

    private String role;

    /** 失败时填充：业务错误码（参考 ErrorCode）*/
    private Integer errorCode;

    /** 失败时填充：人类可读错误消息 */
    private String errorMsg;
}
