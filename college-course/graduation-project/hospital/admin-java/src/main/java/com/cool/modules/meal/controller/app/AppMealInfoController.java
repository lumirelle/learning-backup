package com.cool.modules.meal.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.enums.QueryModeEnum;
import com.cool.core.request.CrudOption;
import com.cool.core.request.PageResult;
import com.cool.core.request.R;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.modules.meal.entity.MealInfoEntity;
import com.cool.modules.meal.service.MealInfoService;
import com.cool.modules.user.entity.UserInfoEntity;
import com.cool.modules.user.service.UserInfoService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import static com.cool.modules.meal.entity.table.MealInfoEntityTableDef.MEAL_INFO_ENTITY;

/**
 * 套餐信息
 */
@Tag(name = "套餐信息", description = "套餐信息")
@CoolRestController(api = {"page", "info"})
public class AppMealInfoController extends BaseController<MealInfoService, MealInfoEntity> {

    private final UserInfoService userInfoService;

    public AppMealInfoController(UserInfoService userInfoService) {
        super();
        this.userInfoService = userInfoService;
    }

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        UserInfoEntity userInfoEntity = userInfoService.getById(CoolSecurityUtil.getCurrentUserId());
        requestParams.set("userRole", userInfoEntity.getRole());

        // 设置分页查询条件
        setPageOption(createOp()
            .keyWordLikeFields(
                MEAL_INFO_ENTITY.NAME,
                MEAL_INFO_ENTITY.INTRO
            )
            .fieldEq(
                MEAL_INFO_ENTITY.STATUS,
                MEAL_INFO_ENTITY.CATEGORY_ID,
                MEAL_INFO_ENTITY.HOSPITAL_ID,
                MEAL_INFO_ENTITY.DEPARTMENT_ID,
                MEAL_INFO_ENTITY.DOCTOR_ID
            )
        );
    }
}
