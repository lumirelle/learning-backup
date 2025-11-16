package com.cool.modules.patient.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 就诊记录
 */
@Getter
@Setter
@Table(value = "medical_record", comment = "就诊记录")
public class MedicalRecordEntity extends BaseEntity<MedicalRecordEntity> {

    @Index
    @ColumnDefine(comment = "就诊日期")
    private Date visitDate;

    @ColumnDefine(comment = "医院 ID")
    private Integer hospitalId;

    @Column(ignore = true)
    private String hospitalName;

    @ColumnDefine(comment = "医生 ID")
    private String doctorId;

    @Column(ignore = true)
    private String doctorName;

    @ColumnDefine(comment = "诊断结果")
    private String diagnosis;

    @ColumnDefine(comment = "处方内容")
    private String prescription;

    @ColumnDefine(comment = "费用")
    private BigDecimal cost;

    @ColumnDefine(comment = "患者ID")
    private Long patientId;

    @Column(ignore = true)
    private String patientName;

}
