package com.dorm.service.dorm;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.enums.dorm.DormStatus;
import com.dorm.mapper.dorm.DormMapper;
import com.dorm.entity.dorm.BuildingSettingVO;
import com.dorm.entity.dorm.DormPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
public class DormServiceImpl extends ServiceImpl<DormMapper, DormPO> implements DormService {

    @Override
    public boolean isDormExist(String building, String no) {
        QueryWrapper<DormPO> qw = new QueryWrapper<>();
        qw.eq("building", building).eq("no", no);
        return count(qw) > 0;
    }

    @Override
    public List<DormPO> listFreeDorms() {
        QueryWrapper<DormPO> qw = new QueryWrapper<>();
        qw.eq("status", DormStatus.FREE);
        return list(qw);
    }

    @Override
    public Set<String> listUniqueBuildings() {
        //toset()实现去重
        return list().stream().map(DormPO::getBuilding).collect(Collectors.toSet());
    }

    @Override
    public List<BuildingSettingVO> countPeoplePerBuild() {
        return baseMapper.listSettingPerBuilding();
    }

}
