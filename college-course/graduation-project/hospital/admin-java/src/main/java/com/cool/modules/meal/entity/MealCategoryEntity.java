package com.cool.modules.meal.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;

/**
 * 套餐分类表
 */
@Getter
@Setter
@Table(value = "meal_category", comment = "套餐分类表")
public class MealCategoryEntity extends BaseEntity<MealCategoryEntity> {

    @UniIndex(name = "uni_name")
    @ColumnDefine(comment = "名称")
    private String name;

    @ColumnDefine(comment = "状态 0-禁用 1-启用", defaultValue = "1")
    private Integer status;

    @ColumnDefine(comment = "排序")
    private Integer sort;

}
