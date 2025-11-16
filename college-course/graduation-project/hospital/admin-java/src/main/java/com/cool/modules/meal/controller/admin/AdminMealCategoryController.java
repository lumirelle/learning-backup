package com.cool.modules.meal.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.meal.entity.MealCategoryEntity;
import com.cool.modules.meal.service.MealCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestAttribute;

import static com.cool.modules.meal.entity.table.MealCategoryEntityTableDef.MEAL_CATEGORY_ENTITY;

/**
 * 套餐分类管理
 */
@CoolRestController(api = {"add", "delete", "update", "info", "page"})
public class AdminMealCategoryController extends BaseController<MealCategoryService, MealCategoryEntity> {

    @Override
    protected void init(HttpServletRequest request, @RequestAttribute JSONObject requestParams) {
        setPageOption(
            createOp()
                .keyWordLikeFields(MEAL_CATEGORY_ENTITY.NAME)
                .fieldEq(MEAL_CATEGORY_ENTITY.STATUS, MEAL_CATEGORY_ENTITY.STATUS)
        );
    }
}
