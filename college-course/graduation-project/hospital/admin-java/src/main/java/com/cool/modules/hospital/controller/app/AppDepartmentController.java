package com.cool.modules.hospital.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.hospital.entity.DepartmentEntity;
import com.cool.modules.hospital.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.hospital.entity.table.DepartmentEntityTableDef.DEPARTMENT_ENTITY;

/**
 * 科室信息
 */
@Tag(name = "科室信息", description = "科室信息")
@CoolRestController(api = {"page", "info"})
public class AppDepartmentController extends BaseController<DepartmentService, DepartmentEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 设置分页查询条件
        setPageOption(createOp()
            .keyWordLikeFields(
                DEPARTMENT_ENTITY.NAME,
                DEPARTMENT_ENTITY.CODE
            )
            .fieldEq(
                DEPARTMENT_ENTITY.HOSPITAL_ID,
                DEPARTMENT_ENTITY.TYPE,
                DEPARTMENT_ENTITY.STATUS
            )
        );
    }
} 