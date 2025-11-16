package com.cool.modules.accompany.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.accompany.entity.AccompanyStaffEntity;
import com.cool.modules.accompany.service.AccompanyStaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 陪诊人档案管理
 */
@Tag(name = "陪诊人档案管理", description = "管理陪诊人档案信息")
@CoolRestController(api = {"info", "page"})
public class AppAccompanyStaffController extends BaseController<AccompanyStaffService, AccompanyStaffEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
    }
}
