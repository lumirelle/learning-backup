package com.ats.organization;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.entity.Department;
import com.ats.entity.RootOrg;
import com.ats.entity.SubDepartment;
import com.ats.organization.dto.SubDepartmentCreateReq;
import com.ats.repository.DepartmentMapper;
import com.ats.repository.HrSubDepartmentMapper;
import com.ats.repository.RootOrgMapper;
import com.ats.repository.SubDepartmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService · 组织树")
class OrganizationServiceTest {

    @Mock RootOrgMapper rootOrgMapper;
    @Mock DepartmentMapper departmentMapper;
    @Mock SubDepartmentMapper subDepartmentMapper;
    @Mock HrSubDepartmentMapper hrSubDepartmentMapper;

    @InjectMocks OrganizationService organizationService;

    @Test
    @DisplayName("删除仍有岗位的子部门 → SUB_DEPARTMENT_HAS_JOBS")
    void deleteSubDepartment_withJobs() {
        SubDepartment sd = new SubDepartment();
        sd.setId(3L);
        when(subDepartmentMapper.selectById(3L)).thenReturn(sd);
        when(subDepartmentMapper.countActiveJobs(3L)).thenReturn(2L);

        assertThatThrownBy(() -> organizationService.deleteSubDepartment(3L))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.SUB_DEPARTMENT_HAS_JOBS);
    }

}
