package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.inform.Inform;
import java.util.List;

public interface InformMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(Inform record);

  int insertSelective(Inform record);

  Inform selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(Inform record);

  int updateByPrimaryKey(Inform record);

  List<Inform> selectInform(int course);

  int setTheTop(int id, String top, int course);
}
