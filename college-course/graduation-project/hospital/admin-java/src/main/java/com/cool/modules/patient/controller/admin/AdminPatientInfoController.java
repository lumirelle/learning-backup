package com.cool.modules.patient.controller.admin;

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
@CoolRestController(api = {"add", "delete", "update", "info", "page"})
public class AdminPatientInfoController extends BaseController<PatientInfoService, PatientInfoEntity> {
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(
                PATIENT_INFO_ENTITY.ID,
                PATIENT_INFO_ENTITY.PATIENT_USER_ID,
                PATIENT_INFO_ENTITY.TYPE
            )
            .keyWordLikeFields(
                PATIENT_INFO_ENTITY.NAME,
                PATIENT_INFO_ENTITY.PHONE,
                PATIENT_INFO_ENTITY.ADDRESS,
                PATIENT_INFO_ENTITY.MEDICAL_RECORD_NUMBER
            ));
    }
}
