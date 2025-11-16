package com.cool.modules.meal.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

import java.math.BigDecimal;

/**
 * 服务套餐表
 */
@Getter
@Setter
@Table(value = "meal_info", comment = "套餐信息表")
public class MealInfoEntity extends BaseEntity<MealInfoEntity> {

    @Index(name = "idx_name")
    @ColumnDefine(comment = "名称")
    private String name;

    @ColumnDefine(comment = "价格")
    private BigDecimal price;

    @ColumnDefine(comment = "状态 0-禁用 1-启用", defaultValue = "1")
    private Integer status;

    @ColumnDefine(comment = "分类ID")
    private Long categoryId;

    @Column(ignore = true)
    private String categoryName;

    @ColumnDefine(comment = "医院 ID")
    private Long hospitalId;

    @Column(ignore = true)
    private String hospitalName;

    // 科室
    @ColumnDefine(comment = "科室 ID")
    private Long departmentId;

    @Column(ignore = true)
    private String departmentName;

    // 医生
    @ColumnDefine(comment = "医生 ID")
    private Long doctorId;

    @Column(ignore = true)
    private String doctorName;

    // 陪诊员
    @ColumnDefine(comment = "陪诊员 ID")
    private Long staffId;

    @Column(ignore = true)
    private String staffName;

    @ColumnDefine(comment = "简介", type = "TEXT")
    private String intro;

    @ColumnDefine(comment = "封面图")
    private String cover;

    @ColumnDefine(comment = "服务次数", defaultValue = "0")
    private Integer serviceCount;

    @ColumnDefine(comment = "服务范围 0=代预约 1=代走流程 2=医嘱分析 3=健康跟踪 4=其他定制服务", type = "json")
    private String serviceArea;
}
