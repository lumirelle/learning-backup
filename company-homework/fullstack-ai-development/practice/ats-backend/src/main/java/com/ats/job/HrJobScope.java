package com.ats.job;

import java.util.List;

/**
 * HR 岗位可见范围：自己创建的岗位 + 绑定子部门下的全部岗位。
 * Admin 视角传 {@code null}（不裁剪）。
 */
public record HrJobScope(Long hrUserId, List<Long> subDepartmentIds) {

    public boolean isEmpty() {
        return hrUserId == null;
    }
}
