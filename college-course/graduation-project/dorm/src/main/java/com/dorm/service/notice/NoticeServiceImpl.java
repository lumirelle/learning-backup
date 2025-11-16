package com.dorm.service.notice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.mapper.notice.NoticeMapper;
import com.dorm.entity.notice.NoticePO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, NoticePO> implements NoticeService {

    @Override
    public List<NoticePO> listByCreateTimeDesc() {
        QueryWrapper<NoticePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }
}
