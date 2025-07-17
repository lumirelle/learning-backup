package com.example.classroomschool.service;

import com.example.classroomschool.entity.homework.HomeworkComments;
import com.example.classroomschool.mapper.HomeworkCommentsMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class HomeworkCommentsService {

  @Resource
  HomeworkCommentsMapper homeworkCommentsMapper;

  public List<HomeworkComments> findByHomeworkId(Integer homeworkId) {
    return homeworkCommentsMapper.selectComments(homeworkId);
  }

  public void create(HomeworkComments homeworkComments) {
    homeworkCommentsMapper.insert(homeworkComments);
  }

  public int deleteById(Integer id) {
    return homeworkCommentsMapper.deleteByPrimaryKey(id);
  }

  public int deleteUserComments(String account, Integer homeworkId) {
    return homeworkCommentsMapper.deleteByMap(account, homeworkId);
  }

  public int deleteByHomeworkId(Integer homeworkId) {
    return homeworkCommentsMapper.deleteComments(homeworkId);
  }
}
