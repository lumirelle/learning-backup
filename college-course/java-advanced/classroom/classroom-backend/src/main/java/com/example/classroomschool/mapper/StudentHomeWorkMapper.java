package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.homework.StudentHomework;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentHomeWorkMapper {

  List<StudentHomework> selectHomework(int homeworkId);
}
