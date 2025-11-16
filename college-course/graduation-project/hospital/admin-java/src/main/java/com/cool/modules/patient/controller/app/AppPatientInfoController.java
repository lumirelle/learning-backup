package com.cool.modules.patient.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.patient.entity.PatientInfoEntity;
import com.cool.modules.patient.service.PatientInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.patient.entity.table.PatientInfoEntityTableDef.PATIENT_INFO_ENTITY;

/**
 * 患者档案管理
 */
@Tag(name = "患者档案管理", description = "管理患者档案信息")
@CoolRestController(api = "info")
public class AppPatientInfoController extends BaseController<PatientInfoService, PatientInfoEntity> {
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
    }
}
