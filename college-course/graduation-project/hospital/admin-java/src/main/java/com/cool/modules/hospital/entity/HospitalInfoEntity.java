package com.cool.modules.hospital.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.ColumnType;

import static org.dromara.autotable.annotation.mysql.MysqlTypeConstant.TEXT;

/**
 * 医院信息
 */
@Getter
@Setter
@Table(value = "hospital_info", comment = "医院信息")
public class HospitalInfoEntity extends BaseEntity<HospitalInfoEntity> {

    @ColumnDefine(comment = "名称")
    private String name;

    @UniIndex
    @ColumnDefine(comment = "编码")
    private String code;

    @ColumnDefine(comment = "地址")
    private String address;

    @ColumnDefine(comment = "联系电话")
    private String phone;

    @ColumnDefine(comment = "状态 0-禁用 1-启用", defaultValue = "1")
    private Integer status;

    @ColumnDefine(comment = "封面图")
    private String coverImage;

    @ColumnDefine(comment = "详细图")
    private String detailImage;

    @ColumnDefine(comment = "医院简介", type = "TEXT")
    private String introduction;

}
