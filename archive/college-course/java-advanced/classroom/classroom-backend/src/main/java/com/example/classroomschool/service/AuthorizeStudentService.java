package com.example.classroomschool.service;

import com.example.classroomschool.entity.student.AuthorizeStudent;
import com.example.classroomschool.mapper.AuthorizeStudentMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AuthorizeStudentService {

  @Resource
  AuthorizeStudentMapper authorizeStudentMapper;

  public AuthorizeStudent findByAccountKey(String accountKey) {
    return authorizeStudentMapper.selectStudent(accountKey);
  }

  public AuthorizeStudent findByMailbox(String mailbox) {
    return authorizeStudentMapper.selectMailbox(mailbox);
  }

  public AuthorizeStudent findByPhone(String phone) {
    return authorizeStudentMapper.selectPhone(phone);
  }

  public AuthorizeStudent findByAccount(String account) {
    return authorizeStudentMapper.selectAccount(account);
  }

  public int createStudent(AuthorizeStudent authorizeStudent) {
    return authorizeStudentMapper.insert(authorizeStudent);
  }

  public AuthorizeStudent findById(Integer student) {
    return authorizeStudentMapper.selectByPrimaryKey(student);
  }
}
