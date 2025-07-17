package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.student.AuthorizeStudent;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizeStudentMapper {

//    int deleteByPrimaryKey(Integer id);

  int insert(AuthorizeStudent record);

  int insertSelective(AuthorizeStudent record);

  AuthorizeStudent selectByPrimaryKey(Integer id);

  AuthorizeStudent selectStudent(String key);

  AuthorizeStudent selectMailbox(String mailbox);

  AuthorizeStudent selectPhone(String phone);

  AuthorizeStudent selectAccount(String account);

  int updateByPrimaryKeySelective(AuthorizeStudent record);

  int updateByPrimaryKey(AuthorizeStudent record);
}
