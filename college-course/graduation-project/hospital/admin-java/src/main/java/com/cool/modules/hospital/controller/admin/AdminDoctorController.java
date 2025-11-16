package com.cool.modules.hospital.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.hospital.entity.DoctorEntity;
import com.cool.modules.hospital.service.DoctorService;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.hospital.entity.table.DoctorEntityTableDef.DOCTOR_ENTITY;

/**
 * 医生信息
 */
@CoolRestController(api = {"add", "delete", "update", "info", "page"})
public class AdminDoctorController extends BaseController<DoctorService, DoctorEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .keyWordLikeFields(
                DOCTOR_ENTITY.NAME,
                DOCTOR_ENTITY.JOB_CODE,
                DOCTOR_ENTITY.TITLE
            )
            .fieldEq(
                DOCTOR_ENTITY.ID,
                DOCTOR_ENTITY.HOSPITAL_ID,
                DOCTOR_ENTITY.DEPARTMENT_ID,
                DOCTOR_ENTITY.STATUS
            ));
    }
}
