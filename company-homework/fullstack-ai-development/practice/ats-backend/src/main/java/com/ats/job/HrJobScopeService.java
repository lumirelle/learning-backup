package com.ats.job;

import com.ats.common.security.SecurityUtil;
import com.ats.entity.Job;
import com.ats.repository.HrSubDepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HrJobScopeService {

    private final HrSubDepartmentMapper hrSubDepartmentMapper;

    /** Admin / 非 HR 返回 null；HR 返回本人 id + 绑定子部门列表（可为空）。 */
    public HrJobScope currentScopeOrNull() {
        if (!SecurityUtil.isHr()) {
            return null;
        }
        Long uid = SecurityUtil.requireUserId();
        List<Long> subIds = hrSubDepartmentMapper.selectSubDepartmentIdsByUserId(uid);
        return new HrJobScope(uid, subIds == null ? List.of() : subIds);
    }

    public boolean canManageJob(Job job) {
        if (job == null) {
            return false;
        }
        if (SecurityUtil.isAdmin()) {
            return true;
        }
        if (!SecurityUtil.isHr()) {
            return false;
        }
        Long uid = SecurityUtil.requireUserId();
        if (Objects.equals(job.getCreatedBy(), uid)) {
            return true;
        }
        HrJobScope scope = currentScopeOrNull();
        if (scope == null || job.getSubDepartmentId() == null) {
            return false;
        }
        return scope.subDepartmentIds().contains(job.getSubDepartmentId());
    }
}
