package com.dorm.entity.dorm.move;

import com.dorm.enums.dorm.move.MoveStatus;
import com.dorm.enums.dorm.move.MoveType;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class MoveVO {
    // from MovePO
    private Integer id;
    private MoveType type;
    private Integer studentId;
    private Integer fromDormId;
    private Integer toDormId;
    private String prove;
    private MoveStatus status;
    private Date createTime;
    private Date updateTime;

    // from StudentPO
    private String name;

    // from fromDormPO
    private String fromBuilding;
    private String fromNo;

    // from toDormPO
    private String toBuilding;
    private String toNo;

    public static MoveVO valueOf(@NonNull MovePO movePO, StudentPO studentPO, DormPO fromDormPO, DormPO toDormPO) {
        MoveVO moveVO = BeanConvertUtils.convert(MoveVO.class, movePO, studentPO);
        // 字段名称不一样，手动设置
        if (fromDormPO != null) {
            moveVO.setFromBuilding(fromDormPO.getBuilding());
            moveVO.setFromNo(fromDormPO.getNo());
        }
        if (toDormPO != null) {
            moveVO.setToBuilding(toDormPO.getBuilding());
            moveVO.setToNo(toDormPO.getNo());
        }
        return moveVO;
    }

}
