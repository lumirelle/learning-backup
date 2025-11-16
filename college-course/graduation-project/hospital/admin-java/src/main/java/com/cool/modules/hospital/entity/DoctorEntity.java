package com.cool.modules.hospital.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

/**
 * 医生信息
 */
@Getter
@Setter
@Table(value = "hospital_doctor", comment = "医生信息")
public class DoctorEntity extends BaseEntity<DoctorEntity> {

    @ColumnDefine(comment = "姓名")
    private String name;

    @UniIndex
    @ColumnDefine(comment = "工号")
    private String jobCode;

    @ColumnDefine(comment = "职称")
    private String title;

    @Index
    @ColumnDefine(comment = "医院ID")
    private Long hospitalId;

    @Column(ignore = true)
    private String hospitalName;

    @Index
    @ColumnDefine(comment = "科室ID（关联科室）")
    private Long departmentId;

    @Column(ignore = true)
    private String departmentName;

    @ColumnDefine(comment = "专长")
    private String specialty;

    @ColumnDefine(comment = "状态 0-禁用 1-启用", defaultValue = "1")
    private Integer status;

    // 简介
    @ColumnDefine(comment = "简介", type = "TEXT")
    private String introduction;

    // 头像
    @ColumnDefine(comment = "头像")
    private String avatar;

}
