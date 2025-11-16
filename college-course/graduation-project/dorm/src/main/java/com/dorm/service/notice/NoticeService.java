package com.dorm.service.notice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.notice.NoticePO;

import java.util.List;

public interface NoticeService extends IService<NoticePO> {

    List<NoticePO> listByCreateTimeDesc();

}
