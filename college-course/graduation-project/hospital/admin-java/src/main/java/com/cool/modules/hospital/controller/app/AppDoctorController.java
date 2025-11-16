package com.cool.modules.hospital.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.hospital.entity.DoctorEntity;
import com.cool.modules.hospital.service.DoctorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.hospital.entity.table.DoctorEntityTableDef.DOCTOR_ENTITY;

/**
 * 医生信息
 */
@Tag(name = "医生信息", description = "医生信息")
@CoolRestController(api = {"page", "info"})
public class AppDoctorController extends BaseController<DoctorService, DoctorEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 设置分页查询条件
        setPageOption(createOp()
            .keyWordLikeFields(
                DOCTOR_ENTITY.NAME,
                DOCTOR_ENTITY.JOB_CODE,
                DOCTOR_ENTITY.TITLE
            )
            .fieldEq(
                DOCTOR_ENTITY.HOSPITAL_ID,
                DOCTOR_ENTITY.DEPARTMENT_ID
            )
        );
    }
} 