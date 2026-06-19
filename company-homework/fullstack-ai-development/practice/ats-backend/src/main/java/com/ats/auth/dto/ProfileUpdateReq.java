package com.ats.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateReq {

    @Size(max = 100)
    private String fullName;

    /** 候选人兴趣标签 id 列表（如 fe / pm） */
    private List<String> interests;
}
