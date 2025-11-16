package com.dorm.entity.dorm.access;

import com.dorm.enums.dorm.access.AccessStatus;
import com.dorm.enums.dorm.access.AccessType;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class AccessVO {

    // from AccessPO
    private Integer id;
    private AccessType type;
    private String reason;
    private String source;
    private String destination;
    private Date createTime;
    private Date updateTime;
    private AccessStatus status;
    private Integer studentId;

    // from StudentPO
    private String no;
    private String name;
    private String college;
    private String major;
    private Integer dormId;

    // from DormPO
    private String dorm;

    public static AccessVO valueOf(@NonNull AccessPO accessPO, StudentPO studentPO, DormPO dormPO) {
        AccessVO accessVO = BeanConvertUtils.convert(AccessVO.class, accessPO, studentPO);
        if (dormPO != null) {
            accessVO.setDorm(dormPO.getBuilding() + ' ' + dormPO.getNo());
        }
        return accessVO;
    }

}
