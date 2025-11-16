package com.cool.modules.meal.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.meal.entity.MealInfoEntity;
import com.cool.modules.meal.service.MealInfoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import static com.cool.modules.meal.entity.table.MealInfoEntityTableDef.MEAL_INFO_ENTITY;

/**
 * 服务套餐管理
 */
@CoolRestController(api = {"add", "delete", "update", "info", "page"})
public class AdminMealInfoController extends BaseController<MealInfoService, MealInfoEntity> {

    @Override
    protected void init(HttpServletRequest request, @RequestAttribute JSONObject requestParams) {
        setPageOption(createOp()
            .keyWordLikeFields(
                MEAL_INFO_ENTITY.NAME
            )
            .fieldEq(
                MEAL_INFO_ENTITY.ID,
                MEAL_INFO_ENTITY.STATUS,
                MEAL_INFO_ENTITY.CATEGORY_ID,
                MEAL_INFO_ENTITY.HOSPITAL_ID,
                MEAL_INFO_ENTITY.DEPARTMENT_ID,
                MEAL_INFO_ENTITY.DOCTOR_ID,
                MEAL_INFO_ENTITY.STAFF_ID
            )
        );
    }

    @Override
    @Operation(summary = "新增", description = "新增信息，对应后端的实体类")
    @PostMapping("/add")
    protected R add(@RequestAttribute() JSONObject requestParams) {
        try {
            return super.add(requestParams);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @Override
    @Operation(summary = "修改", description = "根据ID修改")
    @PostMapping("/update")
    protected R update(@RequestBody MealInfoEntity entity, @RequestAttribute() JSONObject requestParams) {
        try {
            return super.update(entity, requestParams);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}
