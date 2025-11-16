package com.dorm.service.dorm.objects;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.dorm.objects.ObjectsPO;
import com.dorm.mapper.dorm.objects.ObjectsMapper;
import org.springframework.stereotype.Service;

@Service
public class ObjectsServiceImpl extends ServiceImpl<ObjectsMapper, ObjectsPO> implements ObjectsService {
}
