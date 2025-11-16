package com.cool.modules.hospital.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.hospital.entity.DepartmentEntity;
import com.cool.modules.hospital.entity.DoctorEntity;
import com.cool.modules.hospital.entity.HospitalInfoEntity;
import com.cool.modules.hospital.entity.table.DepartmentEntityTableDef;
import com.cool.modules.hospital.entity.table.DoctorEntityTableDef;
import com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef;
import com.cool.modules.hospital.mapper.DepartmentMapper;
import com.cool.modules.hospital.service.DepartmentService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.cool.modules.hospital.entity.table.DepartmentEntityTableDef.DEPARTMENT_ENTITY;
import static com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef.HOSPITAL_INFO_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends BaseServiceImpl<DepartmentMapper, DepartmentEntity> implements DepartmentService {

    @Override
    public Object page(JSONObject requestParams, Page<DepartmentEntity> page, QueryWrapper queryWrapper) {
        queryWrapper.select(
                DEPARTMENT_ENTITY.ALL_COLUMNS,
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName"),
                HOSPITAL_INFO_ENTITY.CODE.as("hospitalCode")
            )
            .from(DEPARTMENT_ENTITY)
            .leftJoin(HOSPITAL_INFO_ENTITY).on(DEPARTMENT_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID));
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public Object info(Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(
                DEPARTMENT_ENTITY.ALL_COLUMNS,
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName"),
                HOSPITAL_INFO_ENTITY.CODE.as("hospitalCode")
            )
            .from(DEPARTMENT_ENTITY)
            .leftJoin(HOSPITAL_INFO_ENTITY).on(DEPARTMENT_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID))
            .where(DEPARTMENT_ENTITY.ID.eq(id));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }
}
