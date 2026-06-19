package com.ats.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserReq {

    @Size(max = 100)
    private String fullName;

    @Pattern(regexp = "HR|CANDIDATE", message = "role 仅允许 HR 或 CANDIDATE")
    private String role;

    private Boolean active;

    /** HR 重绑子部门；传空列表表示清空绑定 */
    private List<Long> subDepartmentIds;

    @Size(min = 8, max = 72)
    private String newPassword;
}
