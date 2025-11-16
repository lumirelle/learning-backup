package com.dorm.entity.dorm.fix;

import com.dorm.enums.dorm.fix.FixStatus;
import com.dorm.enums.dorm.fix.FixType;
import com.dorm.entity.dorm.DormPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class FixVO {
    // From FixPO
    private Integer id;
    private FixType type;
    private String description;
    private String image;
    private FixStatus status;
    private Date createTime;
    private Date updateTime;
    private Integer dormId;

    // From DormPO
    private String dorm;

    public static FixVO valueOf(@NonNull FixPO fixPO) {
        return valueOf(fixPO, null);
    }

    public static FixVO valueOf(@NonNull FixPO fixPO, DormPO dormPO) {
        FixVO fixVO = BeanConvertUtils.convert(FixVO.class, fixPO);
        if (dormPO != null) {
            fixVO.setDorm(dormPO.getBuilding() + " " + dormPO.getNo());
        }
        return fixVO;
    }

}
