package com.cool.modules.patient.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.patient.entity.PatientInfoEntity;
import com.cool.modules.patient.mapper.PatientInfoMapper;
import com.cool.modules.patient.service.PatientInfoService;
import com.cool.modules.user.entity.UserInfoEntity;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import static com.cool.modules.patient.entity.table.PatientInfoEntityTableDef.PATIENT_INFO_ENTITY;
import static com.cool.modules.user.entity.table.UserInfoEntityTableDef.USER_INFO_ENTITY;

/**
 * 患者档案Service实现类
 */
@Service
public class PatientInfoServiceImpl extends BaseServiceImpl<PatientInfoMapper, PatientInfoEntity> implements PatientInfoService {

    // EXAMPLE：多表联查
    @Override
    public Object page(JSONObject requestParams, Page<PatientInfoEntity> page, QueryWrapper queryWrapper) {
        queryWrapper.select(
                PATIENT_INFO_ENTITY.ALL_COLUMNS,
                USER_INFO_ENTITY.NICK_NAME
            )
            .from(PATIENT_INFO_ENTITY)
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID));
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public Object info(Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(
                PATIENT_INFO_ENTITY.ALL_COLUMNS,
                USER_INFO_ENTITY.NICK_NAME
            )
            .from(PATIENT_INFO_ENTITY)
            .leftJoin(USER_INFO_ENTITY).on(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(USER_INFO_ENTITY.ID))
            .where(PATIENT_INFO_ENTITY.ID.eq(id));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }
}
