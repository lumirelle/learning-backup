package com.ats.application.dto;

import com.ats.entity.ApplicationStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StageTransitionReq {

    @NotNull(message = "目标阶段必填")
    private ApplicationStage toStage;

    /** 流转备注，REJECTED 时必填。 */
    @Size(max = 1000, message = "备注过长")
    private String note;
}
