package com.example.classroomschool.service;

import com.example.classroomschool.entity.teacher.AuthorizeTeacher;
import com.example.classroomschool.mapper.AuthorizeTeacherMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AuthorizeTeacherService {

  @Resource
  AuthorizeTeacherMapper authorizeTeacherMapper;

  public AuthorizeTeacher findByAccountKey(String accountKey) {
    return authorizeTeacherMapper.selectTeacher(accountKey);
  }

  public AuthorizeTeacher findByMailbox(String mailbox) {
    return authorizeTeacherMapper.selectMailbox(mailbox);
  }

  public AuthorizeTeacher findByPhone(String telephone) {
    return authorizeTeacherMapper.selectPhone(telephone);
  }

  public AuthorizeTeacher findByAccount(String account) {
    return authorizeTeacherMapper.selectAccount(account);
  }

  public int createTeacher(AuthorizeTeacher authorizeTeacher) {
    return authorizeTeacherMapper.insert(authorizeTeacher);
  }

  public AuthorizeTeacher findById(int id) {
    return authorizeTeacherMapper.selectByPrimaryKey(id);
  }
}
