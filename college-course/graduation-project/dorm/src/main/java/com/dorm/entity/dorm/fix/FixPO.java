package com.dorm.entity.dorm.fix;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.dorm.DormPO;
import com.dorm.enums.dorm.fix.FixStatus;
import com.dorm.enums.dorm.fix.FixType;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("dorm_fix")
public class FixPO {
    private Integer id;
    private FixType type;
    private String description;
    private String image;
    private FixStatus status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // 关联宿舍
    private Integer dormId;

    public static FixPO valueOf(AddFixDTO addFixDTO) {
        return BeanConvertUtils.convert(FixPO.class, addFixDTO);
    }

}
