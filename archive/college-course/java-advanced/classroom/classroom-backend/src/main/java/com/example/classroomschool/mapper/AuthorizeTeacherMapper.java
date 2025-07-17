package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.teacher.AuthorizeTeacher;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizeTeacherMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(AuthorizeTeacher record);

  int insertSelective(AuthorizeTeacher record);

  AuthorizeTeacher selectByPrimaryKey(Integer id);

  AuthorizeTeacher selectTeacher(String account);

  AuthorizeTeacher selectMailbox(String mailbox);

  AuthorizeTeacher selectPhone(String phone);

  AuthorizeTeacher selectAccount(String account);

  int updateByPrimaryKeySelective(AuthorizeTeacher record);

  int updateByPrimaryKey(AuthorizeTeacher record);
}
