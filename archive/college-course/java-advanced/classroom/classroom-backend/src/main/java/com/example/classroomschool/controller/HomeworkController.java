package com.example.classroomschool.controller;

import com.example.classroomschool.entity.homework.Homework;
import com.example.classroomschool.entity.homework.StudentSubmitHomeworkFile;
import com.example.classroomschool.service.HomeworkService;
import com.example.classroomschool.service.QiNiuService;
import com.example.classroomschool.service.StudentSubmitHomeworkFileService;
import com.example.classroomschool.util.pinyin.Chines2PingUtil;
import com.qiniu.common.QiniuException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 作业文件上传下载删除
 */
@RestController
@RequestMapping("/api/v1")
public class HomeworkController {

  @Resource
  QiNiuService qiNiuService;

  @Resource
  HomeworkService homeworkService;

  @Resource
  StudentSubmitHomeworkFileService studentSubmitHomeworkFileService;


  /**
   * 删除作业文件
   */
  @RequestMapping("/delete")
  public String delete(@RequestBody Map<String, String> data) throws QiniuException {
    String name = data.get("studentId") + "-" + data.get("homeworkId") + "-" + data.get("name");
    qiNiuService.delete(Chines2PingUtil.getFullSpell(name));
    studentSubmitHomeworkFileService.delete(Integer.parseInt(data.get("id")));
    return "删除成功";
  }

  /**
   * 上传作业文件
   */
  @RequestMapping("/upload")
  public String upload(@RequestParam("homeworkIds") String homeworkIdSting, @RequestParam("studentIds") String studentIdSting,
      @RequestParam MultipartFile file) throws Exception {
    int homeworkId = Integer.parseInt(homeworkIdSting);
    Homework homework = homeworkService.findById(homeworkId);

    // 判断是否超时
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");//设置日期格式
    if ("1".equals(homework.getOvertime())) { // 1 表示设置了作业超时不能提交
      ParsePosition position = new ParsePosition(0);
      if (!new Date().before(dateFormat.parse(homework.getEndTime(), position))) {
        return "超时不能提交";
      }
    }

    StudentSubmitHomeworkFile studentSubmitHomeworkFile = new StudentSubmitHomeworkFile();
    // 上传文件大小以kb为单位，向下取整保留两位小数
    studentSubmitHomeworkFile.setFileSize(
        new BigDecimal(file.getSize() / 1024).setScale(2, RoundingMode.DOWN).doubleValue());
    studentSubmitHomeworkFile.setStudent(studentIdSting);
    studentSubmitHomeworkFile.setHomework(homeworkId);
    studentSubmitHomeworkFile.setFilename(file.getOriginalFilename());
    // 上传文件到七牛云，并且保存文件的url
    studentSubmitHomeworkFile.setFileurl(qiNiuService.uploadFile(homeworkIdSting, studentIdSting, file));
    studentSubmitHomeworkFileService.handIn(studentSubmitHomeworkFile);
    return "提交成功";
  }

  /**
   * 下载作业
   */
  @RequestMapping("/file/{filename}")
  public String download(@PathVariable("filename") String filename) {
    if (filename.isEmpty()) {
      return "null"; // 文件名为空，返回字符串表示空而不是真实的null
    }
    try {
      return qiNiuService.getPublicFile(filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
