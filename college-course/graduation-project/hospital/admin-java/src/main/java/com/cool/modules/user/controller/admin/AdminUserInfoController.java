package com.cool.modules.user.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.user.entity.UserInfoEntity;
import com.cool.modules.user.service.UserInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.user.entity.table.UserInfoEntityTableDef.USER_INFO_ENTITY;

import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "用户信息", description = "用户信息")
@CoolRestController(api = {"delete", "update", "page", "list", "info", "count"})
public class AdminUserInfoController extends BaseController<UserInfoService, UserInfoEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp().fieldEq(
                USER_INFO_ENTITY.ID,
                USER_INFO_ENTITY.UNIONID,
                USER_INFO_ENTITY.ROLE,
                USER_INFO_ENTITY.STATUS,
                USER_INFO_ENTITY.LOGIN_TYPE
            )
            .keyWordLikeFields(
                USER_INFO_ENTITY.NICK_NAME,
                USER_INFO_ENTITY.PHONE
            ));
    }

    @Operation(summary = "统计", description = "统计用户总量")
    @GetMapping("/count")
    public R<Long> count() {
        return R.ok(service.count());
    }

    @Operation(summary = "统计", description = "统计日增总量")
    @GetMapping("/countToday")
    public R<Long> countToday() {
        return R.ok(service.countToday());
    }
}
