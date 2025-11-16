package com.dorm.entity.board;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.UserPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("board")
public class BoardPO {
    private Integer id;
    private String content;
    private Integer userId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private Integer rootBoardId;
    private Integer parentBoardId;

    public static BoardPO valueOf(@NonNull AddBoardDTO addBoardDTO) {
        return BeanConvertUtils.convert(BoardPO.class, addBoardDTO);
    }
}
