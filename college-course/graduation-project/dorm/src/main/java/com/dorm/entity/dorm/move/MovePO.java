package com.dorm.entity.dorm.move;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.dorm.DormPO;
import com.dorm.enums.dorm.move.MoveStatus;
import com.dorm.enums.dorm.move.MoveType;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("dorm_move")
public class MovePO {
    private Integer id;
    private MoveType type;

    private Integer studentId;

    /**
     * 从哪个宿舍迁出
     */
    private Integer fromDormId;

    /**
     * 迁入哪个宿舍
     */
    private Integer toDormId;

    /**
     * 证明材料链接
     */
    private String prove;

    private MoveStatus status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public static MovePO valueOf(@NonNull AddMoveDTO addMoveDTO) {
        return BeanConvertUtils.convert(MovePO.class, addMoveDTO);
    }

}
