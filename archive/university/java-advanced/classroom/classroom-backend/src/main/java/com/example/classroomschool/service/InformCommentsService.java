package com.example.classroomschool.service;

import com.example.classroomschool.entity.inform.InformComments;
import com.example.classroomschool.mapper.InformCommentsMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class InformCommentsService {

  @Resource
  InformCommentsMapper informCommentsMapper;

  public List<InformComments> findByInformId(int informId) {
    return informCommentsMapper.selectComments(informId);
  }

  public void createInformComments(InformComments comments) {
    informCommentsMapper.insert(comments);
  }

  public int deleteById(Integer id) {
    return informCommentsMapper.deleteByPrimaryKey(id);
  }

  public int deleteUserComments(String account, Integer informId) {
    return informCommentsMapper.deleteByMap(account, informId);
  }

  public int deleteByInformId(Integer informId) {
    return informCommentsMapper.deleteInformComment(informId);
  }
}
