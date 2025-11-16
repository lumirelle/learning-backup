package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.picture.Picture;

public interface PictureMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(Picture record);

  int insertSelective(Picture record);

  Picture selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(Picture record);

  int updateByPrimaryKey(Picture record);

  Picture selectAddress(String account);
}
