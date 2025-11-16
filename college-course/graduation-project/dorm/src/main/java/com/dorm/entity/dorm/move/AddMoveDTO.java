package com.dorm.entity.dorm.move;

import com.dorm.enums.dorm.move.MoveType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMoveDTO {
    @NotNull(message = "搬迁类型不能为空")
    private MoveType type;

    @NotNull(message = "学生id不能为空")
    private Integer studentId;

    private Integer toDormId;

}
