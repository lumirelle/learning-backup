package com.ats.job.dto;

import com.ats.entity.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobTransitionReq {
    /** 目标状态，合法流转矩阵见 JobStatusMachine */
    @NotNull
    private JobStatus to;
}
