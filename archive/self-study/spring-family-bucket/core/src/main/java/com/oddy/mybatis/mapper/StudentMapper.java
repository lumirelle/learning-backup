package com.oddy.mybatis.mapper;

import com.oddy.mybatis.entity.Student;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

// SpringBoot 中可以使用 @Mapper 注解
//@Mapper
public interface StudentMapper {

  // 指定 id，此后可以通过 @ResultMap("student") 来引用
  // 减少重复代码
  @Results(id = "student", value = {
      @Result(property = "sId", column = "s_id"),
      @Result(property = "sName", column = "s_name")
  })
  @Select("select * from student where s_id = #{sId}")
  Student selectStudentById(Integer sId);

  @ResultMap("student")
  @Select("select * from student")
  Student[] selectAllStudents();

  @ResultMap("student")
  @Insert("insert into student(s_name) values(#{sName})")
  void insertStudent(Student student);

}
