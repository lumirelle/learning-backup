package com.ats.repository;

import com.ats.entity.RootOrg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 根组织 Mapper。业务上只读，仅作为外键归属。
 */
@Mapper
public interface RootOrgMapper extends BaseMapper<RootOrg> {
}
