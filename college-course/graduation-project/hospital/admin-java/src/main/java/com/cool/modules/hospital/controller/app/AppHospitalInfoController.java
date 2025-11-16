package com.cool.modules.hospital.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.hospital.entity.HospitalInfoEntity;
import com.cool.modules.hospital.service.HospitalInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef.HOSPITAL_INFO_ENTITY;

/**
 * 医院信息
 */
@Tag(name = "医院信息", description = "医院信息")
@CoolRestController(api = {"page", "info"})
public class AppHospitalInfoController extends BaseController<HospitalInfoService, HospitalInfoEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 设置分页查询条件
        setPageOption(createOp()
            .keyWordLikeFields(
                HOSPITAL_INFO_ENTITY.NAME,
                HOSPITAL_INFO_ENTITY.CODE,
                HOSPITAL_INFO_ENTITY.ADDRESS,
                HOSPITAL_INFO_ENTITY.PHONE
            )
            .fieldEq(
                HOSPITAL_INFO_ENTITY.ID,
                HOSPITAL_INFO_ENTITY.STATUS
            )
        );
    }
} 