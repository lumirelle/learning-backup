package com.cool.modules.order.service.impl;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.modules.feedback.mapper.ComplaintMapper;
import com.cool.modules.meal.entity.MealInfoEntity;
import com.cool.modules.meal.mapper.MealInfoMapper;
import com.cool.modules.order.entity.OrderInfoEntity;
import com.cool.modules.order.mapper.OrderInfoMapper;
import com.cool.modules.order.service.OrderInfoService;
import com.cool.modules.patient.entity.PatientInfoEntity;
import com.cool.modules.patient.mapper.PatientInfoMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;
import static com.cool.modules.feedback.entity.table.ComplaintEntityTableDef.COMPLAINT_ENTITY;
import static com.cool.modules.meal.entity.table.MealInfoEntityTableDef.MEAL_INFO_ENTITY;
import static com.cool.modules.order.entity.table.OrderInfoEntityTableDef.ORDER_INFO_ENTITY;
import static com.cool.modules.patient.entity.table.PatientInfoEntityTableDef.PATIENT_INFO_ENTITY;
import static com.cool.modules.user.entity.table.UserInfoEntityTableDef.USER_INFO_ENTITY;

/**
 * 订单信息ServiceImpl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderInfoServiceImpl extends BaseServiceImpl<OrderInfoMapper, OrderInfoEntity> implements
    OrderInfoService {

    private final PatientInfoMapper patientInfoMapper;

    @Override
    public Object page(JSONObject requestParams, Page<OrderInfoEntity> page, QueryWrapper queryWrapper) {
        queryWrapper.select(
                ORDER_INFO_ENTITY.ALL_COLUMNS,
                MEAL_INFO_ENTITY.NAME.as("mealName"),
                MEAL_INFO_ENTITY.COVER.as("mealCover"),
                PATIENT_INFO_ENTITY.NAME.as("patientName")
            )
            .from(ORDER_INFO_ENTITY)
            .leftJoin(PATIENT_INFO_ENTITY).on(ORDER_INFO_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID));
        return mapper.paginateWithRelations(page, queryWrapper, fieldQueryBuilder -> {
            fieldQueryBuilder.field(OrderInfoEntity::getHasComplaint)
                .queryWrapper(orderInfo ->
                    QueryWrapper.create()
                        .select("CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END")
                        .from(COMPLAINT_ENTITY)
                        .where(COMPLAINT_ENTITY.ORDER_ID.eq(orderInfo.getId()))
                );
        });
    }

    @Override
    public Object info(Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(
                ORDER_INFO_ENTITY.ALL_COLUMNS,
                MEAL_INFO_ENTITY.NAME.as("mealName"),
                MEAL_INFO_ENTITY.COVER.as("mealCover"),
                PATIENT_INFO_ENTITY.NAME.as("patientName")
            )
            .leftJoin(PATIENT_INFO_ENTITY).on(ORDER_INFO_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID))
            .where(ORDER_INFO_ENTITY.ID.eq(id));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }

    @Override
    public Long countByUserId(Long userId) {
        return count(QueryWrapper.create()
            .from(ORDER_INFO_ENTITY)
            .leftJoin(PATIENT_INFO_ENTITY).on(ORDER_INFO_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID))
            .where(
                PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId)
                    .or(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
            )
        );
    }

    @Override
    public Long countWaitingPaymentByUserId(Long userId) {
        return count(QueryWrapper.create()
            .from(ORDER_INFO_ENTITY)
            .leftJoin(PATIENT_INFO_ENTITY).on(ORDER_INFO_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID))
            .where(
                PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId)
                    .or(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
            )
            .and(ORDER_INFO_ENTITY.STATUS.eq(0))
        );
    }

    @Override
    public Long countWaitingUseByUserId(Long userId) {
        return count(QueryWrapper.create()
            .from(ORDER_INFO_ENTITY)
            .leftJoin(PATIENT_INFO_ENTITY).on(ORDER_INFO_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID))
            .where(
                PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId)
                    .or(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
            )
            .and(ORDER_INFO_ENTITY.STATUS.eq(2))
        );
    }

    @Override
    public Long countCompleteByUserId(Long userId) {
        return count(QueryWrapper.create()
            .from(ORDER_INFO_ENTITY)
            .leftJoin(PATIENT_INFO_ENTITY).on(ORDER_INFO_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(MEAL_INFO_ENTITY).on(ORDER_INFO_ENTITY.MEAL_ID.eq(MEAL_INFO_ENTITY.ID))
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(MEAL_INFO_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID))
            .where(
                PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId)
                    .or(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
            )
            .and(ORDER_INFO_ENTITY.STATUS.eq(3))
        );
    }

    @Override
    public Long countPayed() {
        return count(QueryWrapper.create()
            .from(ORDER_INFO_ENTITY)
            .where(ORDER_INFO_ENTITY.STATUS.ge(1))
            .and(ORDER_INFO_ENTITY.STATUS.le(3))
        );
    }

    @Override
    public BigDecimal sumAmount() {
        Map map = mapper.selectOneByQueryAs(
            QueryWrapper.create()
                .select(QueryMethods.sum(ORDER_INFO_ENTITY.ACTUAL_AMOUNT).as("result")),
            Map.class
        );
        return (BigDecimal) map.get("result");
    }

    @Override
    public Long add(OrderInfoEntity entity) {
        // NO + 6位随机数 + 当前时间戳
        entity.setOrderNumber("NO" + RandomUtil.randomString("0123456789", 6) + System.currentTimeMillis());
        // 获取当前用户的患者信息
        Long userId = CoolSecurityUtil.getCurrentUserId();
        PatientInfoEntity patientInfoEntity = patientInfoMapper.selectOneByQuery(
            QueryWrapper.create()
                .where(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId))
        );
        entity.setPatientId(patientInfoEntity.getId());
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void writeOff(Long id, String verifyCode) {
        if (verifyCode == null || verifyCode.isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }
        OrderInfoEntity orderInfoEntity = getById(id);
        if (orderInfoEntity.getStatus() != 2) {
            throw new RuntimeException("订单状态错误");
        }
        if (!Objects.equals(orderInfoEntity.getVerifyCode(), verifyCode)) {
            throw new RuntimeException("验证码错误");
        }
        orderInfoEntity.setStatus(3);
        update(orderInfoEntity);

        // 记录服务次数
        UpdateChain.of(MealInfoEntity.class)
            .setRaw(MEAL_INFO_ENTITY.SERVICE_COUNT, "service_count + 1")
            .where(MEAL_INFO_ENTITY.ID.eq(orderInfoEntity.getMealId()))
            .update();
    }

}
