package com.dorm.entity.notice;

import com.dorm.entity.user.UserPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class NoticeVO {
    // from NoticePO
    private Integer id;
    private String title;
    private String content;
    private Date createTime;
    private Integer userId;

    // from UserPO
    private String username;
    private String email;
    private String phone;
    private String avatar;

    public static NoticeVO valueOf(@NonNull NoticePO noticePO, UserPO userPO) {
        return BeanConvertUtils.convert(NoticeVO.class, noticePO, userPO);
    }
}
