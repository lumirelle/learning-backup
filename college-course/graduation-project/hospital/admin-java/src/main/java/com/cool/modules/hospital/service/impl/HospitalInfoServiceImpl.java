package com.cool.modules.hospital.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.hospital.entity.HospitalInfoEntity;
import com.cool.modules.hospital.mapper.HospitalInfoMapper;
import com.cool.modules.hospital.service.HospitalInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalInfoServiceImpl extends BaseServiceImpl<HospitalInfoMapper, HospitalInfoEntity> implements HospitalInfoService {

}