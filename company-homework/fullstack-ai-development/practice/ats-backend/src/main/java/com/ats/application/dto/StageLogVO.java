package com.ats.application.dto;

import com.ats.entity.ApplicationStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageLogVO {
    private Long id;
    /** null 表示初始投递（候选人入口） */
    private ApplicationStage fromStage;
    private ApplicationStage toStage;
    private String note;
    private Long operatedBy;
    private String operatedByName;
    /** ADMIN / HR / CANDIDATE，方便前端时间线区分操作人角色色 */
    private String operatedByRole;
    private OffsetDateTime operatedAt;
}
