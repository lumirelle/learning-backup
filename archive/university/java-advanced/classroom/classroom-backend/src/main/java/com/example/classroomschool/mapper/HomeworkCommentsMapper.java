package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.homework.HomeworkComments;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeworkCommentsMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(HomeworkComments record);

  int insertSelective(HomeworkComments record);

  HomeworkComments selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(HomeworkComments record);

  int updateByPrimaryKey(HomeworkComments record);

  List<HomeworkComments> selectComments(Integer homeworkId);

  int deleteByMap(String account, Integer homeworkId);

  int deleteComments(Integer homeworkId);
}
