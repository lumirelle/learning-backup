package com.ats.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MeVO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private List<String> interests;
}
