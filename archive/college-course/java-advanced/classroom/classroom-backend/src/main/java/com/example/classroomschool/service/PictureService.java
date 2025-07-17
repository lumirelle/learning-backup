package com.example.classroomschool.service;

import com.example.classroomschool.entity.picture.Picture;
import com.example.classroomschool.mapper.PictureMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PictureService {

  @Resource
  PictureMapper pictureMapper;

  public int createPicture(Picture picture) {
    return pictureMapper.insert(picture);
  }

  public Picture findById(String picid) {
    return pictureMapper.selectAddress(picid);
  }
}
