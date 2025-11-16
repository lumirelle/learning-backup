package com.cool.modules.patient.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

import java.util.Date;

/**
 * 患者档案
 */
@Getter
@Setter
@Table(value = "patient_info", comment = "患者档案")
public class PatientInfoEntity extends BaseEntity<PatientInfoEntity> {

    @ColumnDefine(comment = "姓名")
    private String name;

    @ColumnDefine(comment = "性别 0-未知 1-男 2-女", defaultValue = "0")
    private Integer gender;

    @ColumnDefine(comment = "生日")
    private Date birthday;

    @UniIndex
    @ColumnDefine(comment = "电话")
    private String phone;

    @ColumnDefine(comment = "地址")
    private String address;

    @ColumnDefine(comment = "类型 0=正常 1=沟通不便 2=行动不便 3=其他不便", defaultValue = "0")
    private Integer type;

    @UniIndex
    @ColumnDefine(comment = "病历号")
    private String medicalRecordNumber;

    @ColumnDefine(comment = "病史")
    private String medicalHistory;

    @ColumnDefine(comment = "过敏史")
    private String allergyHistory;

    @ColumnDefine(comment = "备注")
    private String remark;

    @ColumnDefine(comment = "身高(cm)")
    private Integer height;

    @ColumnDefine(comment = "体重(kg)")
    private Integer weight;

    @ColumnDefine(comment = "收缩压(mmHg)")
    private Integer systolicPressure;

    @ColumnDefine(comment = "舒张压(mmHg)")
    private Integer diastolicPressure;

    @Index
    @ColumnDefine(comment = "关联用户ID")
    private Long patientUserId;

    @Column(ignore = true)
    private String nickName;

}
