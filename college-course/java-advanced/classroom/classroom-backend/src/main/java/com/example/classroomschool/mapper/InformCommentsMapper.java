package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.inform.InformComments;
import java.util.List;

public interface InformCommentsMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(InformComments record);

  int insertSelective(InformComments record);

  InformComments selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(InformComments record);

  int updateByPrimaryKey(InformComments record);

  List<InformComments> selectComments(int informId);

  int deleteByMap(String account, Integer informId);

  int deleteInformComment(Integer informId);
}
