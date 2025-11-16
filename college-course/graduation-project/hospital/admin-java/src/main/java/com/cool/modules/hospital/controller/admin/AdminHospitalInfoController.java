package com.cool.modules.hospital.controller.admin;

import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.hospital.entity.HospitalInfoEntity;
import com.cool.modules.hospital.service.HospitalInfoService;
import jakarta.servlet.http.HttpServletRequest;
import cn.hutool.json.JSONObject;

import static com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef.HOSPITAL_INFO_ENTITY;

/**
 * 医院信息
 */
@CoolRestController(api = {"add", "delete", "update", "info", "page"})
public class AdminHospitalInfoController extends BaseController<HospitalInfoService, HospitalInfoEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
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
            ));
    }
}