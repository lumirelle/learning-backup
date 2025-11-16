package com.cool.modules.meal.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.meal.entity.MealCategoryEntity;
import com.cool.modules.meal.service.MealCategoryService;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.meal.entity.table.MealCategoryEntityTableDef.MEAL_CATEGORY_ENTITY;

/**
 * 套餐分类
 */
@Tag(name = "套餐分类", description = "套餐分类")
@CoolRestController(api = {"page", "info"})
public class AppMealCategoryController extends BaseController<MealCategoryService, MealCategoryEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 设置分页查询条件
        setPageOption(createOp()
            .keyWordLikeFields(
                MEAL_CATEGORY_ENTITY.NAME
            )
            .fieldEq(
                MEAL_CATEGORY_ENTITY.STATUS
            )
        );
    }
} 
