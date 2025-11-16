package com.cool.modules.dict.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "dict_info", comment = "字典信息")
public class DictInfoEntity extends BaseEntity<DictInfoEntity> {

    @ColumnDefine(comment = "类型ID", notNull = true)
    private Long typeId;

    @ColumnDefine(comment = "父ID")
    private Long parentId;

    @ColumnDefine(comment = "名称", notNull = true)
    private String name;

    @ColumnDefine(comment = "值")
    private String value;

    @ColumnDefine(comment = "字典信息样式（primary、success、info、warning、danger）", defaultValue =  "primary")
    private String type;

    @ColumnDefine(comment = "排序", defaultValue = "0")
    private Integer orderNum;

    @ColumnDefine(comment = "备注")
    private String remark;

}
