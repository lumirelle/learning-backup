package com.cool.modules.meal.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.modules.accompany.entity.AccompanyStaffEntity;
import com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef;
import com.cool.modules.accompany.mapper.AccompanyStaffMapper;
import com.cool.modules.hospital.entity.DepartmentEntity;
import com.cool.modules.hospital.entity.DoctorEntity;
import com.cool.modules.hospital.entity.HospitalInfoEntity;
import com.cool.modules.hospital.entity.table.DepartmentEntityTableDef;
import com.cool.modules.hospital.entity.table.DoctorEntityTableDef;
import com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef;
import com.cool.modules.meal.entity.MealCategoryEntity;
import com.cool.modules.meal.entity.MealInfoEntity;
import com.cool.modules.meal.entity.table.MealCategoryEntityTableDef;
import com.cool.modules.meal.entity.table.MealInfoEntityTableDef;
import com.cool.modules.meal.mapper.MealInfoMapper;
import com.cool.modules.meal.service.MealInfoService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;
import static com.cool.modules.hospital.entity.table.DepartmentEntityTableDef.DEPARTMENT_ENTITY;
import static com.cool.modules.hospital.entity.table.DoctorEntityTableDef.DOCTOR_ENTITY;
import static com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef.HOSPITAL_INFO_ENTITY;
import static com.cool.modules.meal.entity.table.MealCategoryEntityTableDef.MEAL_CATEGORY_ENTITY;
import static com.cool.modules.meal.entity.table.MealInfoEntityTableDef.MEAL_INFO_ENTITY;

@Service
@RequiredArgsConstructor
public class MealInfoServiceImpl extends BaseServiceImpl<MealInfoMapper, MealInfoEntity> implements MealInfoService {

    private final AccompanyStaffMapper accompanyStaffMapper;
    private final MealInfoMapper mealInfoMapper;

    @Override
    public Object page(JSONObject requestParams, Page<MealInfoEntity> page, QueryWrapper queryWrapper) {
        queryWrapper
            .select(
                MEAL_INFO_ENTITY.ALL_COLUMNS,
                MEAL_CATEGORY_ENTITY.NAME.as("categoryName"),
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName"),
                DEPARTMENT_ENTITY.NAME.as("departmentName"),
                DOCTOR_ENTITY.NAME.as("doctorName"),
                ACCOMPANY_STAFF_ENTITY.NAME.as("staffName")
            )
            .from(MEAL_INFO_ENTITY)
            .leftJoin(MEAL_CATEGORY_ENTITY).on(MEAL_INFO_ENTITY.CATEGORY_ID.eq(MEAL_CATEGORY_ENTITY.ID))
            .leftJoin(HOSPITAL_INFO_ENTITY).on(MEAL_INFO_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID))
            .leftJoin(DEPARTMENT_ENTITY).on(MEAL_INFO_ENTITY.DEPARTMENT_ID.eq(DEPARTMENT_ENTITY.ID))
            .leftJoin(DOCTOR_ENTITY).on(MEAL_INFO_ENTITY.DOCTOR_ID.eq(DOCTOR_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            // 如果用户是陪诊人员（userRole == 2），则只查询自己的数据
            .where(
                ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(CoolSecurityUtil.getCurrentUserId()).when(requestParams.containsKey("userRole") && requestParams.getInt("userRole") == 2)
            );
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public Object info(Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(
                MEAL_INFO_ENTITY.ALL_COLUMNS,
                MEAL_CATEGORY_ENTITY.NAME.as("categoryName"),
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName"),
                DEPARTMENT_ENTITY.NAME.as("departmentName"),
                DOCTOR_ENTITY.NAME.as("doctorName"),
                ACCOMPANY_STAFF_ENTITY.NAME.as("staffName")
            )
            .from(MEAL_INFO_ENTITY)
            .leftJoin(MEAL_CATEGORY_ENTITY).on(MEAL_INFO_ENTITY.CATEGORY_ID.eq(MEAL_CATEGORY_ENTITY.ID))
            .leftJoin(HOSPITAL_INFO_ENTITY).on(MEAL_INFO_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID))
            .leftJoin(DEPARTMENT_ENTITY).on(MEAL_INFO_ENTITY.DEPARTMENT_ID.eq(DEPARTMENT_ENTITY.ID))
            .leftJoin(DOCTOR_ENTITY).on(MEAL_INFO_ENTITY.DOCTOR_ID.eq(DOCTOR_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .where(MEAL_INFO_ENTITY.ID.eq(id));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }

    @Override
    public Long add(MealInfoEntity entity) {
        long mealsCount = mealInfoMapper.selectCountByQuery(
            QueryWrapper.create()
                .select(MEAL_INFO_ENTITY.ID)
                .from(MEAL_INFO_ENTITY)
                .where(MEAL_INFO_ENTITY.STAFF_ID.eq(entity.getStaffId()))
        );

        // 添加套餐限制 20 个
        if (mealsCount >= 20) {
            throw new RuntimeException("套餐数量已达 20 个的上限！");
        }

        AccompanyStaffEntity accompanyStaffEntity = accompanyStaffMapper.selectOneById(entity.getStaffId());

        // 根据已启用套餐数量限制套餐启用状态
        long enableMealsCount = mealInfoMapper.selectCountByQuery(
            QueryWrapper.create()
                .select(MEAL_INFO_ENTITY.ID)
                .from(MEAL_INFO_ENTITY)
                .where(MEAL_INFO_ENTITY.STAFF_ID.eq(entity.getStaffId()))
                .and(MEAL_INFO_ENTITY.STATUS.eq(1))
        );
        if (accompanyStaffEntity.getLevel() == 0 && enableMealsCount >= 3) {
            entity.setStatus(0);
        } else if (accompanyStaffEntity.getLevel() == 1 && enableMealsCount >= 5) {
            entity.setStatus(0);
        } else if (accompanyStaffEntity.getLevel() == 2 && enableMealsCount >= 10) {
            entity.setStatus(0);
        }

        return super.add(entity);
    }

    @Override
    public boolean update(MealInfoEntity entity) {
        // 修改的套餐状态是禁用的，无需判断
        if (entity.getStatus() == 0) {
            return super.update(entity);
        }

        AccompanyStaffEntity accompanyStaffEntity = accompanyStaffMapper.selectOneById(entity.getStaffId());

        // 根据价格限制套餐启用状态
        if (accompanyStaffEntity.getLevel() == 0 && entity.getPrice().compareTo(new BigDecimal(100)) > 0) {
            throw new RuntimeException("初级陪诊员套餐价格不能超过 100 元！");
        } else if (accompanyStaffEntity.getLevel() == 1 && entity.getPrice().compareTo(new BigDecimal(1000)) > 0) {
            throw new RuntimeException("中级陪诊员套餐价格不能超过 1000 元！");
        }

        // 根据已启用套餐数量限制套餐启用状态
        long enableMealsCount = mealInfoMapper.selectCountByQuery(
            QueryWrapper.create()
                .select(MEAL_INFO_ENTITY.ID)
                .from(MEAL_INFO_ENTITY)
                .where(MEAL_INFO_ENTITY.STAFF_ID.eq(entity.getStaffId()))
                .and(MEAL_INFO_ENTITY.STATUS.eq(1))
        );
        if (accompanyStaffEntity.getLevel() == 0 && enableMealsCount >= 3) {
            throw new RuntimeException("初级陪诊员只能启用 3 个套餐！");
        } else if (accompanyStaffEntity.getLevel() == 1 && enableMealsCount >= 5) {
            throw new RuntimeException("中级陪诊员只能启用 5 个套餐！");
        } else if (accompanyStaffEntity.getLevel() == 2 && enableMealsCount >= 10) {
            throw new RuntimeException("高级陪诊员只能启用 10 个套餐！");
        }

        return super.update(entity);
    }
}
