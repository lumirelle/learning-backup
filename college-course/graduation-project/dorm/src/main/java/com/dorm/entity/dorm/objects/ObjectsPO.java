package com.dorm.entity.dorm.objects;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.dorm.objects.ObjectsType;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
@TableName("dorm_objects")
public class ObjectsPO {
    private Integer id;
    private String no;
    private ObjectsType type;
    private String description;
    private Integer studentId;
    private Date createTime;

    public static ObjectsPO valueOf(@NonNull AddObjectsDTO objectsDTO) {
        return BeanConvertUtils.convert(ObjectsPO.class, objectsDTO);
    }

}
