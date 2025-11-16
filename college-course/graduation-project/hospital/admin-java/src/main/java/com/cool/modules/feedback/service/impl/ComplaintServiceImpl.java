package com.cool.modules.feedback.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.modules.accompany.entity.AccompanyStaffEntity;
import com.cool.modules.accompany.mapper.AccompanyStaffMapper;
import com.cool.modules.feedback.entity.ComplaintEntity;
import com.cool.modules.feedback.mapper.ComplaintMapper;
import com.cool.modules.feedback.service.ComplaintService;
import com.cool.modules.meal.entity.MealInfoEntity;
import com.cool.modules.meal.mapper.MealInfoMapper;
import com.cool.modules.order.entity.OrderInfoEntity;
import com.cool.modules.order.mapper.OrderInfoMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;
import static com.cool.modules.base.entity.sys.table.BaseSysUserEntityTableDef.BASE_SYS_USER_ENTITY;
import static com.cool.modules.feedback.entity.table.ComplaintEntityTableDef.COMPLAINT_ENTITY;
import static com.cool.modules.meal.entity.table.MealInfoEntityTableDef.MEAL_INFO_ENTITY;
import static com.cool.modules.order.entity.table.OrderInfoEntityTableDef.ORDER_INFO_ENTITY;
import static com.cool.modules.user.entity.table.UserInfoEntityTableDef.USER_INFO_ENTITY;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 投诉信息服务实现类
 */
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl extends BaseServiceImpl<ComplaintMapper, ComplaintEntity> implements
    ComplaintService {

    private final AccompanyStaffMapper accompanyStaffMapper;
    private final MealInfoMapper mealInfoMapper;
    private final OrderInfoMapper orderInfoMapper;

    @Override
    public Object page(JSONObject requestParams, Page<ComplaintEntity> page, QueryWrapper queryWrapper) {
        queryWrapper.select(
                COMPLAINT_ENTITY.ALL_COLUMNS,
                ORDER_INFO_ENTITY.ORDER_NUMBER.as("orderNumber"),
                USER_INFO_ENTITY.NICK_NAME.as("userNickName"),
                BASE_SYS_USER_ENTITY.NICK_NAME.as("handlerNickName")
            )
            .from(COMPLAINT_ENTITY)
            .leftJoin(ORDER_INFO_ENTITY).on(COMPLAINT_ENTITY.ORDER_ID.eq(ORDER_INFO_ENTITY.ID))
            .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .leftJoin(USER_INFO_ENTITY).on(USER_INFO_ENTITY.ID.eq(COMPLAINT_ENTITY.COMPLAINT_USER_ID))
            .leftJoin(BASE_SYS_USER_ENTITY).on(BASE_SYS_USER_ENTITY.ID.eq(COMPLAINT_ENTITY.HANDLER_ID))
            .where(
                ACCOMPANY_STAFF_ENTITY.ID.eq(requestParams.getInt("staffId"))
                    .when(requestParams.containsKey("staffId"))
            );
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public Object info(Long id) {
        QueryWrapper queryWrapper = QueryWrapper
            .create()
            .select(
                COMPLAINT_ENTITY.ALL_COLUMNS,
                USER_INFO_ENTITY.NICK_NAME.as("userNickName"),
                BASE_SYS_USER_ENTITY.NICK_NAME.as("handlerNickName")
            )
            .from(COMPLAINT_ENTITY)
            .leftJoin(USER_INFO_ENTITY).on(USER_INFO_ENTITY.ID.eq(COMPLAINT_ENTITY.COMPLAINT_USER_ID))
            .leftJoin(BASE_SYS_USER_ENTITY).on(BASE_SYS_USER_ENTITY.ID.eq(COMPLAINT_ENTITY.HANDLER_ID))
            .where(COMPLAINT_ENTITY.ID.eq(id));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }

    @Override
    public List<Long> countThisYear() {
        List<Long> list = new ArrayList<>();
        Date beginOfYear = DateUtil.beginOfYear(DateUtil.date());
        // 统计每个月的投诉数量
        for (int i = 0; i < 12; i++) {
            Date beginOfIMonth = DateUtil.offsetMonth(beginOfYear, i);
            Date endOfIMonth = DateUtil.endOfMonth(beginOfIMonth);
            list.add(count(QueryWrapper.create()
                .from(COMPLAINT_ENTITY)
                .where(COMPLAINT_ENTITY.CREATE_TIME.ge(beginOfIMonth)
                    .and(COMPLAINT_ENTITY.CREATE_TIME.lt(endOfIMonth)))
                .select(COMPLAINT_ENTITY.ID)));
        }
        return list;
    }

    @Override
    public void modifyAfter(JSONObject requestParams, ComplaintEntity entity) {
        ComplaintEntity complaintEntity = getOneByEntityId(entity);
        OrderInfoEntity orderInfoEntity = orderInfoMapper.selectOneById(complaintEntity.getOrderId());
        MealInfoEntity mealInfoEntity = mealInfoMapper.selectOneById(orderInfoEntity.getMealId());
        AccompanyStaffEntity accompanyStaffEntity = accompanyStaffMapper.selectOneById(mealInfoEntity.getStaffId());

        // 统计陪诊员本周非为解决、已关闭的投诉量
        long complaintCount = count(
            QueryWrapper.create()
                .leftJoin(ORDER_INFO_ENTITY).on(COMPLAINT_ENTITY.ORDER_ID.eq(ORDER_INFO_ENTITY.ID))
                .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
                .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
                .where(ACCOMPANY_STAFF_ENTITY.ID.eq(accompanyStaffEntity.getId()))
                .and(COMPLAINT_ENTITY.STATUS.in(2, 3))
                .and(COMPLAINT_ENTITY.CREATE_TIME.ge(DateUtil.beginOfWeek(DateUtil.date())))
        );

        int oldLevel = accompanyStaffEntity.getLevel();
        // 如果本周投诉量超过 5 则对其降级到初级
        if (complaintCount > 5 && accompanyStaffEntity.getLevel() > 0) {
            accompanyStaffEntity.setLevel(0);
            accompanyStaffMapper.update(accompanyStaffEntity);
        }
        // 如果本周投诉量超过 3 则对其降级到中级
        else if (complaintCount > 3 && accompanyStaffEntity.getLevel() > 1) {
            accompanyStaffEntity.setLevel(1);
            accompanyStaffMapper.update(accompanyStaffEntity);
        }
        int newLevel = accompanyStaffEntity.getLevel();

        // 如果被降级
        if (oldLevel != newLevel) {
            // 根据价格限制套餐启用状态
            List<MealInfoEntity> meals = mealInfoMapper.selectListByQuery(
                QueryWrapper.create()
                    .select(MEAL_INFO_ENTITY.ALL_COLUMNS)
                    .from(MEAL_INFO_ENTITY)
                    .where(MEAL_INFO_ENTITY.STAFF_ID.eq(accompanyStaffEntity.getId()))
            );
            Db.executeBatch(meals, 1000, MealInfoMapper.class, (mapper, i) -> {
                if (newLevel == 0 && i.getPrice().compareTo(new BigDecimal(100)) > 0) {
                    i.setStatus(0);
                } else if (newLevel == 1 && i.getPrice().compareTo(new BigDecimal(1000)) > 0) {
                    i.setStatus(0);
                }
                mapper.update(i);
            });

            // 根据已启用数量限制套餐启用状态
            List<MealInfoEntity> enableMeals = mealInfoMapper.selectListByQuery(
                QueryWrapper.create()
                    .select(MEAL_INFO_ENTITY.ALL_COLUMNS)
                    .from(MEAL_INFO_ENTITY)
                    .where(MEAL_INFO_ENTITY.STAFF_ID.eq(accompanyStaffEntity.getId()))
                    .and(MEAL_INFO_ENTITY.STATUS.eq(1))
            );
            Db.executeBatch(enableMeals.size(), 1000, MealInfoMapper.class, (mapper, index) -> {
                if (newLevel == 0 && index <= 3) {
                    return;
                } else if (newLevel == 1 && index <= 5) {
                    return;
                }
                MealInfoEntity i = enableMeals.get(index);
                i.setStatus(0);
                mapper.update(i);
            });
        }
    }
}
