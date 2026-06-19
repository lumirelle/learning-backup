package com.ats.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class AdminUserListItemVO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private Boolean active;
    private List<Long> subDepartmentIds;
    private OffsetDateTime createdAt;
}
