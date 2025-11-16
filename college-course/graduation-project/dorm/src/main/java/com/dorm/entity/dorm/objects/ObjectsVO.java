package com.dorm.entity.dorm.objects;

import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.dorm.objects.ObjectsType;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class ObjectsVO {
    // from ObjectsPO
    private Integer id;
    private String no;
    private ObjectsType type;
    private String description;
    private Date createTime;
    private Integer studentId;

    // from StudentPO
    private String studentNo;
    private String name;

    // from DormPO
    private String dorm;

    public static ObjectsVO valueOf(@NonNull ObjectsPO objectsPO, StudentPO studentPO, DormPO dormPO) {
        ObjectsVO objectsVO = BeanConvertUtils.convert(ObjectsVO.class, objectsPO, studentPO, dormPO);
        if (studentPO != null) {
            objectsVO.setStudentNo(studentPO.getNo());
        }
        if (dormPO != null) {
            objectsVO.setDorm(dormPO.getBuilding() + ' ' + dormPO.getNo());
        }
        return objectsVO;
    }
}
