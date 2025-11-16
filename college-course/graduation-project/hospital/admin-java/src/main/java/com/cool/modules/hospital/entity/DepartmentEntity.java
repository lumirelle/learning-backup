package com.cool.modules.hospital.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;

/**
 * 科室信息
 */
@Getter
@Setter
@Table(value = "hospital_department", comment = "科室信息")
public class DepartmentEntity extends BaseEntity<DepartmentEntity> {

    @ColumnDefine(comment = "名称")
    private String name;

    @UniIndex
    @ColumnDefine(comment = "编码")
    private String code;

    @ColumnDefine(comment = "类型 0-临床 1-医技 2-辅助", defaultValue = "0")
    private Integer type;

    @ColumnDefine(comment = "状态 0-禁用 1-启用", defaultValue = "1")
    private Integer status;

    @ColumnDefine(comment = "医院ID")
    private Long hospitalId;

    @Column(ignore = true)
    private String hospitalName;

    @Column(ignore = true)
    private String hospitalCode;

    @ColumnDefine(comment = "封面图")
    private String coverImage;

    @ColumnDefine(comment = "简介", type = "TEXT")
    private String introduction;

}
