package com.ats.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentUpdateReq {

    @NotBlank
    @Size(max = 100)
    private String name;
}
