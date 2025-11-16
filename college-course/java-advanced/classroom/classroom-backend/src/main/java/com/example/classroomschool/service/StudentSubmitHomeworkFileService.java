package com.example.classroomschool.service;

import com.example.classroomschool.entity.homework.StudentSubmitHomeworkFile;
import com.example.classroomschool.mapper.StudentSubmitHomeworkFileMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentSubmitHomeworkFileService {

  @Resource
  StudentSubmitHomeworkFileMapper studentSubmitHomeworkFileMapper;

  public List<StudentSubmitHomeworkFile> findUserFiles(String studentId, int homework) {
    return studentSubmitHomeworkFileMapper.selectHomework(studentId, homework);
  }

  public int handIn(StudentSubmitHomeworkFile studentSubmitHomeworkFile) {
    return studentSubmitHomeworkFileMapper.insert(studentSubmitHomeworkFile);
  }

  public int update(StudentSubmitHomeworkFile studentSubmitHomeworkFile) {
    return studentSubmitHomeworkFileMapper.updateByPrimaryKey(studentSubmitHomeworkFile);
  }


  public int delete(int id) {
    return studentSubmitHomeworkFileMapper.deleteByPrimaryKey(id);
  }

  public int deleteUserFiels(String account, Integer id) {
    return studentSubmitHomeworkFileMapper.deleteByMap(account, id);
  }

  public int deleteByHomeworkId(Integer homework) {
    return studentSubmitHomeworkFileMapper.deleteAllFile(homework);
  }

  public List<StudentSubmitHomeworkFile> findByHomeworkId(Integer homework) {
    return studentSubmitHomeworkFileMapper.selectHomeworkByHomeworkId(homework);
  }
}
