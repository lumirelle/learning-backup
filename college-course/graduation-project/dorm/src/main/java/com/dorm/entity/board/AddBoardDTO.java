package com.dorm.entity.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddBoardDTO {
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @NotBlank(message = "留言内容不能为空")
    private String content;

    private Integer rootBoardId; // 根留言ID，可以为空
    private Integer parentBoardId; // 父留言ID，可以为空
}
