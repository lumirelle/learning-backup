package com.cool.modules.patient.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.hospital.entity.DoctorEntity;
import com.cool.modules.hospital.entity.HospitalInfoEntity;
import com.cool.modules.patient.entity.MedicalRecordEntity;
import com.cool.modules.patient.entity.PatientInfoEntity;
import com.cool.modules.patient.mapper.MedicalRecordMapper;
import com.cool.modules.patient.service.MedicalRecordService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import static com.cool.modules.hospital.entity.table.DoctorEntityTableDef.DOCTOR_ENTITY;
import static com.cool.modules.hospital.entity.table.HospitalInfoEntityTableDef.HOSPITAL_INFO_ENTITY;
import static com.cool.modules.patient.entity.table.MedicalRecordEntityTableDef.MEDICAL_RECORD_ENTITY;
import static com.cool.modules.patient.entity.table.PatientInfoEntityTableDef.PATIENT_INFO_ENTITY;

/**
 * 就诊记录Service实现类
 */
@Service
public class MedicalRecordServiceImpl extends BaseServiceImpl<MedicalRecordMapper, MedicalRecordEntity> implements MedicalRecordService {

    // EXAMPLE：多表联查
    @Override
    public Object page(JSONObject requestParams, Page<MedicalRecordEntity> page, QueryWrapper queryWrapper) {
        queryWrapper.select(
                MEDICAL_RECORD_ENTITY.ALL_COLUMNS,
                PATIENT_INFO_ENTITY.NAME.as("patientName"),
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName"),
                DOCTOR_ENTITY.NAME.as("doctorName")
            )
            .from(MEDICAL_RECORD_ENTITY)
            .leftJoin(PATIENT_INFO_ENTITY).on(MEDICAL_RECORD_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(HOSPITAL_INFO_ENTITY).on(MEDICAL_RECORD_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID))
            .leftJoin(DOCTOR_ENTITY).on(MEDICAL_RECORD_ENTITY.DOCTOR_ID.eq(DOCTOR_ENTITY.ID));
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public Object info(JSONObject requestParams, Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(
                MEDICAL_RECORD_ENTITY.ALL_COLUMNS,
                PATIENT_INFO_ENTITY.NAME.as("patientName"),
                HOSPITAL_INFO_ENTITY.NAME.as("hospitalName"),
                DOCTOR_ENTITY.NAME.as("doctorName")
            )
            .from(MEDICAL_RECORD_ENTITY)
            .leftJoin(PATIENT_INFO_ENTITY).on(MEDICAL_RECORD_ENTITY.PATIENT_ID.eq(PATIENT_INFO_ENTITY.ID))
            .leftJoin(HOSPITAL_INFO_ENTITY).on(MEDICAL_RECORD_ENTITY.HOSPITAL_ID.eq(HOSPITAL_INFO_ENTITY.ID))
            .leftJoin(DOCTOR_ENTITY).on(MEDICAL_RECORD_ENTITY.DOCTOR_ID.eq(DOCTOR_ENTITY.ID));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }

}
