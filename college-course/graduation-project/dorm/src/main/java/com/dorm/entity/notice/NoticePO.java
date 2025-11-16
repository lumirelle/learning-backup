package com.dorm.entity.notice;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.UserPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("notice")
public class NoticePO {
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    private String title;
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public static NoticePO valueOf(@NonNull AddNoticeDTO addNoticeDTO) {
        return BeanConvertUtils.convert(NoticePO.class, addNoticeDTO);
    }
}
