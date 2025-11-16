package com.dorm.entity.dorm.access;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.dorm.access.AccessStatus;
import com.dorm.enums.dorm.access.AccessType;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
@TableName("dorm_access")
public class AccessPO {

    private Integer id;

    private Integer studentId;

    private AccessType type;

    private String reason;

    /**
     * 入校的来源地
     */
    private String source;

    /**
     * 出校的目的地
     */
    private String destination;

    private AccessStatus status;

    private Date createTime;

    private Date updateTime;

    public static AccessPO valueOf(@NonNull AddAccessDTO accessDTO) {
        return BeanConvertUtils.convert(AccessPO.class, accessDTO);
    }

}
