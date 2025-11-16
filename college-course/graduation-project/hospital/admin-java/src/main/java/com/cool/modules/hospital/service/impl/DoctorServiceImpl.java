package com.cool.modules.hospital.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.hospital.entity.DepartmentEntity;
import com.cool.modules.hospital.entity.DoctorEntity;
import com.cool.modules.hospital.entity.HospitalInfoEntity;
import com.cool.modules.hospital.entity.table.DepartmentEntityTableDef;
import com.cool.modules.hospital.entity.table.DoctorEntityTableDef;
import com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef;
import com.cool.modules.hospital.mapper.DoctorMapper;
import com.cool.modules.hospital.service.DoctorService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.cool.modules.hospital.entity.table.DepartmentEntityTableDef.DEPARTMENT_ENTITY;
import static com.cool.modules.hospital.entity.table.DoctorEntityTableDef.DOCTOR_ENTITY;
import static com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef.HOSPITAL_INFO_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl extends BaseServiceImpl<DoctorMapper, DoctorEntity> implements DoctorService {

    @Override
    public Object page(JSONObject requestParams, Page<DoctorEntity> page, QueryWrapper queryWrapper) {
        queryWrapper.select(
                DOCTOR_ENTITY.ALL_COLUMNS,
                DEPARTMENT_ENTITY.NAME.as("departmentName"),
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName")
            )
            .from(DOCTOR_ENTITY)
            .leftJoin(HOSPITAL_INFO_ENTITY).on(DOCTOR_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID))
            .leftJoin(DEPARTMENT_ENTITY).on(DOCTOR_ENTITY.DEPARTMENT_ID.eq(DEPARTMENT_ENTITY.ID));
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public Object info(Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(
                DOCTOR_ENTITY.ALL_COLUMNS,
                DEPARTMENT_ENTITY.NAME.as("departmentName"),
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName")
            )
            .from(DOCTOR_ENTITY)
            .leftJoin(HOSPITAL_INFO_ENTITY).on(DOCTOR_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID))
            .leftJoin(DEPARTMENT_ENTITY).on(DOCTOR_ENTITY.DEPARTMENT_ID.eq(DEPARTMENT_ENTITY.ID))
            .where(DOCTOR_ENTITY.ID.eq(id));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }
}
