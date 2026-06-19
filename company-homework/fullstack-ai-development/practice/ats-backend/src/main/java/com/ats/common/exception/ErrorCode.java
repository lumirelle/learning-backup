package com.ats.common.exception;

import lombok.Getter;

/**
 * 业务错误码 · 5 段编码
 * <p>
 * 10xxx — 鉴权/权限
 * 20xxx — 业务规则（状态机、重复投递等）
 * 30xxx — 资源相关（文件、上传等）
 * 40xxx — 参数校验/请求格式
 * 50xxx — 系统内部
 */
@Getter
public enum ErrorCode {

    OK(0, "ok"),

    UNAUTHORIZED(10001, "未登录或登录已过期"),
    FORBIDDEN(10002, "无权限执行此操作"),
    INVALID_TOKEN(10003, "无效或过期的 token"),
    EMAIL_ALREADY_EXISTS(10004, "该邮箱已被注册"),
    INVALID_CREDENTIALS(10005, "邮箱或密码错误"),
    REFRESH_TOKEN_INVALID(10006, "Refresh token 无效或已过期，请重新登录"),
    USER_DISABLED(10007, "账号已被禁用，请联系管理员"),
    TOO_MANY_LOGIN_ATTEMPTS(10008, "登录尝试过多，请稍后再试"),

    BIZ_RULE_VIOLATED(20001, "业务规则校验失败"),
    ILLEGAL_TRANSITION(20002, "非法状态流转"),
    DUPLICATE_APPLICATION(20003, "已投递过此岗位，请勿重复操作"),
    REJECT_REASON_REQUIRED(20004, "拒绝时必须填写原因"),

    JOB_NOT_FOUND(20011, "岗位不存在或已被删除"),
    JOB_ACCESS_DENIED(20012, "无权操作此岗位（仅创建人或管理员可修改）"),
    JOB_NOT_PUBLISHED(20013, "岗位未发布，候选人不可查看"),
    JOB_SALARY_RANGE_INVALID(20014, "薪资下限不能大于上限"),
    TAG_NOT_FOUND(20015, "标签不存在或已被删除"),
    DEPARTMENT_NOT_FOUND(20016, "部门不存在或已被删除"),
    SUB_DEPARTMENT_NOT_FOUND(20017, "子部门不存在或已被删除"),
    SUB_DEPARTMENT_HAS_JOBS(20018, "子部门下仍有岗位，不可删除"),
    DEPARTMENT_HAS_CHILDREN(20019, "部门下仍有子部门或下级部门，不可删除"),

    APPLICATION_NOT_FOUND(20021, "投递记录不存在"),
    APPLICATION_ACCESS_DENIED(20022, "无权操作此投递（仅本人候选人或对应岗位 HR / 管理员可访问）"),
    JOB_NOT_HIRING(20023, "岗位不在招聘中，无法投递"),
    SELF_APPLY_FORBIDDEN(20024, "不能为自己创建的岗位投递"),
    APPLICATION_TERMINATED(20025, "该投递已处于终态（已入职 / 已拒绝），不可再变更"),

    INTERVIEW_NOT_FOUND(20031, "面试评价不存在"),
    INTERVIEW_EDIT_EXPIRED(20032, "面试评价已超过 24 小时编辑窗口，不可修改"),
    INTERVIEW_EDIT_FORBIDDEN(20033, "只有原面试官（24h 内）或管理员可以修改"),

    FILE_TYPE_NOT_ALLOWED(30001, "文件类型不被允许"),
    FILE_TOO_LARGE(30002, "文件超出大小限制"),
    FILE_NOT_FOUND(30003, "文件不存在或无权访问"),

    VALIDATION_FAILED(40001, "请求参数校验失败"),
    BAD_REQUEST(40002, "请求格式错误"),
    NOT_FOUND(40004, "请求的资源/接口不存在"),

    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
