package com.ats.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentCreateReq {

    @NotBlank
    @Size(max = 100)
    private String name;

    /**
     * 父部门 id。null = 直挂根组织下。
     * 非 null 时必须在 departments 表存在（嵌套部门）。
     */
    private Long parentDepartmentId;
}
