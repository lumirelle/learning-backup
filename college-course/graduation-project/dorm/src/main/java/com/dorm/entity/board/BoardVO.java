package com.dorm.entity.board;

import com.dorm.entity.user.UserPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;
import java.util.List;

@Data
public class BoardVO {
    // from BoardPO
    private Integer id;
    private String content;
    private Date createTime;
    private Integer userId;

    private List<BoardVO> children;

    // from UserPO
    private String username;
    private String email;
    private String phone;
    private String avatar;

    public static BoardVO valueOf(@NonNull BoardPO boardPO, UserPO userPO) {
        return BeanConvertUtils.convert(BoardVO.class, boardPO, userPO);
    }
}
